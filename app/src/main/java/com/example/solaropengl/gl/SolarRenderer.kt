package com.example.solaropengl.gl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.solaropengl.R
import com.example.solaropengl.gl.objects.Sphere
import com.example.solaropengl.gl.objects.TexturedQuad
import com.example.solaropengl.gl.shaders.ColorShaderProgram
import com.example.solaropengl.gl.shaders.TextureShaderProgram
import com.example.solaropengl.gl.util.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SolarRenderer : GLSurfaceView.Renderer {

    private var appContext: Context? = null

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)

    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private lateinit var quad: TexturedQuad
    private lateinit var sphere: Sphere

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram

    private var galaxyTextureId: Int = 0

    // тайминг для плавной анимации
    private var lastTimeNs: Long = 0L

    // углы Луны
    private var moonOrbit = 0f
    private var moonSpin = 0f

    // углы планет (орбита и вращение)
    private val planetOrbitAngles = FloatArray(8) { 0f }
    private val planetSpinAngles = FloatArray(8) { 0f }

    // временная матрица Земли (без масштаба) — чтобы Луна была строго в позиции Земли
    private val earthMatrix = FloatArray(16)

    private data class PlanetParams(
        val radius: Float,
        val distance: Float,
        val orbitSpeedDegPerSec: Float,
        val spinSpeedDegPerSec: Float,
        val color: FloatArray
    )

    // Эклиптика XZ (планеты вращаются вокруг Y)
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
        sphere = Sphere(stacks = 24, slices = 24)

        val ctx = appContext
        requireNotNull(ctx) {
            "Context is null. OpenGLScreen must call renderer.setContext(context)."
        }
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
        // dt
        val now = System.nanoTime()
        val dt = ((now - lastTimeNs).coerceAtLeast(0L)) / 1_000_000_000f
        lastTimeNs = now

        // углы планет (все через dt)
        for (i in planets.indices) {
            planetOrbitAngles[i] = (planetOrbitAngles[i] + planets[i].orbitSpeedDegPerSec * dt) % 360f
            planetSpinAngles[i] = (planetSpinAngles[i] + planets[i].spinSpeedDegPerSec * dt) % 360f
        }

        // углы Луны
        moonOrbit = (moonOrbit + 180f * dt) % 360f
        moonSpin = (moonSpin + 300f * dt) % 360f

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
        colorProgram.use()
        drawSphereAtCenter(
            scale = 0.45f,
            r = 1.0f, g = 0.85f, b = 0.2f, a = 1.0f
        )

        // ---------- ПЛАНЕТЫ ----------
        for (i in planets.indices) {
            val p = planets[i]
            val orbitAngle = planetOrbitAngles[i]
            val spinAngle = planetSpinAngles[i]

            Matrix.setIdentityM(modelMatrix, 0)

            // орбита в плоскости эклиптики XZ (вращение вокруг Y)
            Matrix.rotateM(modelMatrix, 0, orbitAngle, 0f, 1f, 0f)
            Matrix.translateM(modelMatrix, 0, p.distance, 0f, 0f)

            // собственное вращение
            Matrix.rotateM(modelMatrix, 0, spinAngle, 0f, 1f, 0f)

            // если это Земля — сохраним её матрицу ДО масштаба (для Луны)
            if (i == 2) {
                System.arraycopy(modelMatrix, 0, earthMatrix, 0, 16)
            }

            // размер планеты
            Matrix.scaleM(modelMatrix, 0, p.radius, p.radius, p.radius)

            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            colorProgram.setUniforms(mvpMatrix, p.color[0], p.color[1], p.color[2], p.color[3])

            sphere.bindPositionAttrib(colorProgram.aPositionLocation)
            sphere.draw()

            // Луна для Земли
            if (i == 2) {
                drawMoonAroundEarthPerpendicular(earthMatrix)
            }
        }
    }

    private fun drawSphereAtCenter(scale: Float, r: Float, g: Float, b: Float, a: Float) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        colorProgram.setUniforms(mvpMatrix, r, g, b, a)

        sphere.bindPositionAttrib(colorProgram.aPositionLocation)
        sphere.draw()
    }

    /**
     * Эклиптика: XZ.
     * Орбита Луны перпендикулярна эклиптике: делаем плоскость YZ.
     */
    private fun drawMoonAroundEarthPerpendicular(earthMatrix: FloatArray) {
        // стартуем от матрицы Земли (позиция Земли в мире)
        System.arraycopy(earthMatrix, 0, modelMatrix, 0, 16)

        // поворачиваем орбитальную плоскость на 90° вокруг X => YZ
        Matrix.rotateM(modelMatrix, 0, 90f, 1f, 0f, 0f)

        // орбита Луны
        Matrix.rotateM(modelMatrix, 0, moonOrbit, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0.45f)

        // вращение Луны вокруг своей оси
        Matrix.rotateM(modelMatrix, 0, moonSpin, 0f, 1f, 0f)

        // размер Луны
        Matrix.scaleM(modelMatrix, 0, 0.06f, 0.06f, 0.06f)

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        // цвет Луны (меняй тут)
        colorProgram.setUniforms(mvpMatrix, 0.85f, 0.85f, 0.88f, 1f)

        sphere.bindPositionAttrib(colorProgram.aPositionLocation)
        sphere.draw()
    }
}
