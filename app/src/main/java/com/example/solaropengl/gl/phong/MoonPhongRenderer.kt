package com.example.solaropengl.gl.phong

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.solaropengl.gl.ContextAwareRenderer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MoonPhongRenderer : GLSurfaceView.Renderer, ContextAwareRenderer {

    override fun setContext(context: Context) {
        // Здесь пока не нужен, но оставляем на будущее (текстура Луны/карта нормалей)
    }

    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val vp = FloatArray(16)

    private val model = FloatArray(16)
    private val mvp = FloatArray(16)

    // normal matrix 3x3
    private val normalMat3 = FloatArray(9)

    private lateinit var sphere: PhongSphere
    private lateinit var program: PhongShaderProgram

    private var angle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        sphere = PhongSphere(32, 32)
        program = PhongShaderProgram()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projection, 0, 45f, aspect, 0.1f, 100f)

        Matrix.setLookAtM(
            view, 0,
            0f, 0f, 3.2f,   // камера
            0f, 0f, 0f,
            0f, 1f, 0f
        )

        Matrix.multiplyMM(vp, 0, projection, 0, view, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // небольшое вращение Луны
        angle = (angle + 0.6f) % 360f

        Matrix.setIdentityM(model, 0)
        Matrix.rotateM(model, 0, angle, 0f, 1f, 0f)
        Matrix.scaleM(model, 0, 0.5f, 0.5f, 0.5f)

        Matrix.multiplyMM(mvp, 0, vp, 0, model, 0)

        // normal matrix = верхний левый 3x3 от model (т.к. только rotate/scale, этого хватит)
        normalMat3[0] = model[0]; normalMat3[1] = model[1]; normalMat3[2] = model[2]
        normalMat3[3] = model[4]; normalMat3[4] = model[5]; normalMat3[5] = model[6]
        normalMat3[6] = model[8]; normalMat3[7] = model[9]; normalMat3[8] = model[10]

        program.use()
        program.setMatrices(mvp, model, normalMat3)

        // свет и материал (Фонг)
        val lightPos = floatArrayOf(2.0f, 1.5f, 2.5f) // источник света сбоку/сверху
        val viewPos = floatArrayOf(0f, 0f, 3.2f)     // камера

        val ambient = floatArrayOf(0.10f, 0.10f, 0.10f)
        val diffuse = floatArrayOf(0.65f, 0.65f, 0.68f)   // "серый" диффузный
        val specular = floatArrayOf(0.35f, 0.35f, 0.35f)  // блик не слишком сильный

        program.setLightAndMaterial(
            lightPos = lightPos,
            viewPos = viewPos,
            ambient = ambient,
            diffuse = diffuse,
            specular = specular,
            shininess = 24f
        )

        sphere.bind(program)
        sphere.draw()
    }
}
