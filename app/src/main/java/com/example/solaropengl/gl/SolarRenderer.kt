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
import kotlin.math.sin

class SolarRenderer : GLSurfaceView.Renderer, ContextAwareRenderer {

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

    // ---------- BLACK HOLE ----------
    private var lensTextureId = 0
    private var bhX = -1.25f            // NDC
    private val bhBaseY = 0.25f         // базовая высота
    private var bhSpeed = 0.32f         // NDC/сек
    private val bhBaseScale = 0.30f     // размер линзы
    private var bhAngle = 0f            // вращение
    private var bhTime = 0f             // время
    // ---------------------------------------------------

    private var lastTimeNs = 0L

    // Луна
    private var moonOrbit = 0f
    private var moonSpin = 0f

    // Планеты
    private val planetOrbitAngles = FloatArray(8)
    private val planetSpinAngles = FloatArray(8)

    // Солнце
    private var sunSpin = 0f

    // позиции тел (для куба)
    private val bodyPos = FloatArray(9 * 3)

    // матрица Земли (для Луны)
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

    override fun setContext(context: Context) {
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

        val ctx = appContext ?: error("Context not set (call renderer.setContext(context))")

        galaxyTextureId = TextureHelper.loadTexture(ctx, R.drawable.galaxy)
        lensTextureId = TextureHelper.loadTexture(ctx, R.drawable.lens)

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

        // планеты
        for (i in planets.indices) {
            planetOrbitAngles[i] = (planetOrbitAngles[i] + planets[i].orbitSpeed * dt) % 360f
            planetSpinAngles[i] = (planetSpinAngles[i] + planets[i].spinSpeed * dt) % 360f
        }

        // луна
        moonOrbit = (moonOrbit + 180f * dt) % 360f
        moonSpin = (moonSpin + 300f * dt) % 360f

        // солнце (медленно)
        sunSpin = (sunSpin + 8f * dt) % 360f

        // линза
        bhTime += dt
        bhAngle = (bhAngle + 28f * dt) % 360f
        val speedNow = bhSpeed * (0.9f + 0.1f * sin(bhTime * 0.8f))
        bhX += speedNow * dt
        if (bhX > 1.25f) bhX = -1.25f

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // ---------- 1) ФОН ----------
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        Matrix.setIdentityM(mvpMatrix, 0)

        textureProgram.use()
        textureProgram.setUniforms(mvpMatrix, galaxyTextureId)
        quad.bindData(textureProgram)
        quad.draw()

        // ---------- 2) ЛИНЗА (поверх фона) ----------
        drawLensOverlay()

        // ---------- 3) 3D СЦЕНА ----------
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // ---------- СОЛНЦЕ ----------
        drawSun()

        // ---------- ПЛАНЕТЫ ----------
        for (i in planets.indices) {
            val p = planets[i]

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.rotateM(modelMatrix, 0, planetOrbitAngles[i], 0f, 1f, 0f)
            Matrix.translateM(modelMatrix, 0, p.distance, 0f, 0f)
            Matrix.rotateM(modelMatrix, 0, planetSpinAngles[i], 0f, 1f, 0f)

            // позиция планеты
            val origin = floatArrayOf(0f, 0f, 0f, 1f)
            val out = FloatArray(4)
            Matrix.multiplyMV(out, 0, modelMatrix, 0, origin, 0)
            putBodyPos(i, out[0], out[1], out[2])

            if (i == 2) {
                System.arraycopy(modelMatrix, 0, earthMatrix, 0, 16)
            }

            Matrix.scaleM(modelMatrix, 0, p.radius, p.radius, p.radius)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

            drawLitSphere(
                model = modelMatrix,
                mvp = mvpMatrix,
                color = p.color
            )

            if (i == 2) drawMoonAroundEarth()
        }

        drawSelectionCube()
    }

    // --------------------------- DRAW HELPERS ---------------------------

    private fun drawLensOverlay() {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        GLES20.glDepthMask(false)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        val y = bhBaseY + 0.06f * sin(bhTime * 0.7f)
        val pulse = 1f + 0.06f * sin(bhTime * 1.6f)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, bhX, y, 0f)
        Matrix.rotateM(modelMatrix, 0, bhAngle, 0f, 0f, 1f)
        Matrix.scaleM(modelMatrix, 0, bhBaseScale * pulse, bhBaseScale * pulse, 1f)

        // NDC => mvp = model
        System.arraycopy(modelMatrix, 0, mvpMatrix, 0, 16)

        textureProgram.use()
        textureProgram.setUniforms(mvpMatrix, lensTextureId)
        quad.bindData(textureProgram)
        quad.draw()

        GLES20.glDepthMask(true)
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun drawSun() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, sunSpin, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.45f, 0.45f, 0.45f)

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        drawLitSphere(
            model = modelMatrix,
            mvp = mvpMatrix,
            color = floatArrayOf(1f, 0.9f, 0.25f, 1f)
        )
    }

    private fun drawMoonAroundEarth() {
        System.arraycopy(earthMatrix, 0, modelMatrix, 0, 16)

        // перпендикулярно эклиптике
        Matrix.rotateM(modelMatrix, 0, 90f, 1f, 0f, 0f)

        // орбита
        Matrix.rotateM(modelMatrix, 0, moonOrbit, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0.45f)

        // позиция луны для куба выбора
        val origin = floatArrayOf(0f, 0f, 0f, 1f)
        val out = FloatArray(4)
        Matrix.multiplyMV(out, 0, modelMatrix, 0, origin, 0)
        putBodyPos(8, out[0], out[1], out[2])

        // вращение луны
        Matrix.rotateM(modelMatrix, 0, moonSpin, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.06f, 0.06f, 0.06f)

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        drawLitSphere(
            model = modelMatrix,
            mvp = mvpMatrix,
            color = floatArrayOf(0.85f, 0.85f, 0.88f, 1f)
        )
    }

    /**
     * Общая отрисовка сферы с освещением (Lambert + ambient внутри шейдера).
     */
    private fun drawLitSphere(model: FloatArray, mvp: FloatArray, color: FloatArray) {
        colorProgram.use()
        colorProgram.setUniforms(
            mvpMatrix = mvp,
            modelMatrix = model,
            r = color[0], g = color[1], b = color[2], a = color[3],
            lightDirX = -0.4f, lightDirY = 0.8f, lightDirZ = 0.6f
        )
        sphere.bindData(colorProgram)
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
        colorProgram.setUniforms(
            mvpMatrix = mvpMatrix,
            modelMatrix = modelMatrix,
            r = 1f, g = 1f, b = 1f, a = 0.25f,
            lightDirX = -0.4f, lightDirY = 0.8f, lightDirZ = 0.6f
        )

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
