package com.example.solaropengl.gl.water

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NeptuneWaterRenderer : GLSurfaceView.Renderer {

    private val mvp = FloatArray(16)

    private lateinit var quad: WaterQuad
    private lateinit var program: WaterShaderProgram

    private var startNs: Long = 0L

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        quad = WaterQuad()
        program = WaterShaderProgram()

        Matrix.setIdentityM(mvp, 0)
        startNs = System.nanoTime()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        // Quad рисуем в NDC => mvp = identity
        Matrix.setIdentityM(mvp, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val t = (System.nanoTime() - startNs) / 1_000_000_000f

        program.use()
        program.setUniforms(mvpMatrix = mvp, timeSec = t)

        quad.bindData(program)
        quad.draw()
    }
}
