package com.example.solaropengl.gl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.solaropengl.R
import com.example.solaropengl.gl.objects.Cube
import com.example.solaropengl.gl.objects.Sphere
import com.example.solaropengl.gl.objects.TexturedQuad
import com.example.solaropengl.gl.shaders.ColorShaderProgram
import com.example.solaropengl.gl.shaders.TextureShaderProgram
import com.example.solaropengl.gl.util.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SolarRenderer : GLSurfaceView.Renderer {

    // 8 планет + Луна
    val bodyCount: Int get() = 9

    @Volatile
    private var selectedBodyIndex: Int = 0
    fun setSelectedBodyIndex(index: Int) {
        selectedBodyIndex = index.coerceIn(0, bodyCount - 1)
    }

    private var appContext: Context? = null

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)

    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private lateinit var quad: TexturedQuad
    private lateinit var sphere: Sphere
    private lateinit var cube: Cube

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram

    private var galaxyTextureId = 0

    private var lastTimeNs = 0L

    // Луна
    private var moonOrbit = 0f
    private var moonSpin = 0f

    // Планеты
    private val planetOrbitAngles = FloatArray(8)
    private val planetSpinAngles = FloatArray(8)

    // позиции тел (для куба)
    private val bodyPos = FloatArray(9 * 3)

    // матрица Земли
    private val earthMatrix = FloatArray(16)

    private data class PlanetParams(
        val radius: Float,
        val distance: Float,
        val orbitSpeed: Float,
        val spinSpeed: Float,
        val color: FloatArray
    )

    private val planets = listOf(
        PlanetParams(0.08f, 1.1f, 80f, 180f, floatArrayOf(0.7f, 0.7f, 0.7f, 1f)), // Mercury
        PlanetParams(0.11f, 1.5f, 60f, 140f, floatArrayOf(0.9f, 0.8f, 0.6f, 1f)), // Venus
        PlanetParams(0.13f, 2.0f, 45f, 220f, floatArrayOf(0.2f, 0.6f, 1.0f, 1f)), // Earth
        PlanetParams(0.10f, 2.5f, 35f, 200f, floatArrayOf(0.9f, 0.4f, 0.3f, 1f)), // Mars
        PlanetParams(0.28f, 3.4f, 20f, 120f, floatArrayOf(0.9f, 0.7f, 0.5f, 1f)), // Jupiter
        PlanetParams(0.24f, 4.2f, 16f, 110f, floatArrayOf(0.9f, 0.9f, 0.7f, 1f)), // Saturn
        PlanetParams(0.18f, 5.0f, 12f,  90f, floatArrayOf(0.6f, 0.8f, 0.9f, 1f)), // Uranus
        PlanetParams(0.17f, 5.7f, 10f,  85f, floatArrayOf(0.2f, 0.4f, 0.9f, 1f))  // Neptune
    )


    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        textureProgram = TextureShaderProgram()
        colorProgram = ColorShaderProgram()

        quad = TexturedQuad()
        sphere = Sphere(24, 24)
        cube = Cube()

        val ctx = appContext ?: error("Context not set")
        galaxyTextureId = TextureHelper.loadTexture(ctx, R.drawable.galaxy)

        lastTimeNs = System.nanoTime()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspect, 0.1f, 100f)

        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 7f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val dt = (now - lastTimeNs).coerceAtLeast(0L) / 1_000_000_000f
        lastTimeNs = now

        for (i in planets.indices) {
            planetOrbitAngles[i] = (planetOrbitAngles[i] + planets[i].orbitSpeed * dt) % 360f
            planetSpinAngles[i]  = (planetSpinAngles[i]  + planets[i].spinSpeed  * dt) % 360f
        }

        moonOrbit = (moonOrbit + 180f * dt) % 360f
        moonSpin  = (moonSpin  + 300f * dt) % 360f

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // ---------- ФОН ----------
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        Matrix.setIdentityM(mvpMatrix, 0)
        textureProgram.use()
        textureProgram.setUniforms(mvpMatrix, galaxyTextureId)
        quad.bindData(textureProgram)
        quad.draw()
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // ---------- СОЛНЦЕ ----------
        drawSphereAtCenter(0.45f, 1f, 0.85f, 0.2f, 1f)

        // ---------- ПЛАНЕТЫ ----------
        for (i in planets.indices) {
            val p = planets[i]

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.rotateM(modelMatrix, 0, planetOrbitAngles[i], 0f, 1f, 0f)
            Matrix.translateM(modelMatrix, 0, p.distance, 0f, 0f)
            Matrix.rotateM(modelMatrix, 0, planetSpinAngles[i], 0f, 1f, 0f)

            // ✅ позиция планеты через матрицу (НЕ через cos/sin)
            val origin = floatArrayOf(0f, 0f, 0f, 1f)
            val out = FloatArray(4)
            Matrix.multiplyMV(out, 0, modelMatrix, 0, origin, 0)
            putBodyPos(i, out[0], out[1], out[2])

            if (i == 2) {
                System.arraycopy(modelMatrix, 0, earthMatrix, 0, 16)
            }

            Matrix.scaleM(modelMatrix, 0, p.radius, p.radius, p.radius)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

            colorProgram.use()
            colorProgram.setUniforms(mvpMatrix, p.color[0], p.color[1], p.color[2], p.color[3])
            sphere.bindPositionAttrib(colorProgram.aPositionLocation)
            sphere.draw()

            if (i == 2) drawMoonAroundEarth()
        }

        drawSelectionCube()
    }

    private fun drawSphereAtCenter(scale: Float, r: Float, g: Float, b: Float, a: Float) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        colorProgram.use()
        colorProgram.setUniforms(mvpMatrix, r, g, b, a)
        sphere.bindPositionAttrib(colorProgram.aPositionLocation)
        sphere.draw()
    }

    private fun drawMoonAroundEarth() {
        System.arraycopy(earthMatrix, 0, modelMatrix, 0, 16)

        Matrix.rotateM(modelMatrix, 0, 90f, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, moonOrbit, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0.45f)

        val origin = floatArrayOf(0f, 0f, 0f, 1f)
        val out = FloatArray(4)
        Matrix.multiplyMV(out, 0, modelMatrix, 0, origin, 0)
        putBodyPos(8, out[0], out[1], out[2])

        Matrix.rotateM(modelMatrix, 0, moonSpin, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.06f, 0.06f, 0.06f)

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        colorProgram.setUniforms(mvpMatrix, 0.85f, 0.85f, 0.88f, 1f)
        sphere.bindPositionAttrib(colorProgram.aPositionLocation)
        sphere.draw()
    }

    private fun drawSelectionCube() {
        val idx = selectedBodyIndex
        val x = bodyPos[idx * 3]
        val y = bodyPos[idx * 3 + 1]
        val z = bodyPos[idx * 3 + 2]

        val baseRadius = if (idx < 8) planets[idx].radius else 0.06f
        val cubeHalfSize = baseRadius * 1.6f

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glDepthMask(false)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.scaleM(modelMatrix, 0, cubeHalfSize, cubeHalfSize, cubeHalfSize)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        colorProgram.use()
        colorProgram.setUniforms(mvpMatrix, 1f, 1f, 1f, 0.25f)
        cube.bindData(colorProgram)
        cube.draw()

        GLES20.glDepthMask(true)
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun putBodyPos(index: Int, x: Float, y: Float, z: Float) {
        val i = index * 3
        bodyPos[i] = x
        bodyPos[i + 1] = y
        bodyPos[i + 2] = z
    }
}
