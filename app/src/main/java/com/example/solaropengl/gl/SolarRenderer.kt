package com.example.solaropengl.gl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.solaropengl.R
import com.example.solaropengl.gl.objects.Cube
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
    private lateinit var cube: Cube

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram

    private var galaxyTextureId: Int = 0

    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        // Включаем depth для куба
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        textureProgram = TextureShaderProgram()
        colorProgram = ColorShaderProgram()

        quad = TexturedQuad()
        cube = Cube()

        val ctx = appContext
        requireNotNull(ctx) {
            "Context is null. Use SolarRenderer().also { it.setContext(context) } in OpenGLScreen."
        }

        galaxyTextureId = TextureHelper.loadTexture(ctx, R.drawable.galaxy)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspect, 0.1f, 100f)

        // Камера
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 4f,   // eye
            0f, 0f, 0f,   // center
            0f, 1f, 0f    // up
        )

        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // ---------- 1) ФОН (full-screen, без перспективы) ----------
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        Matrix.setIdentityM(mvpMatrix, 0)
        textureProgram.use()
        textureProgram.setUniforms(mvpMatrix, galaxyTextureId)
        quad.bindData(textureProgram)
        quad.draw()

        // ---------- 2) КУБ (по центру, нормальный размер) ----------
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, 0.4f, 0.4f, 0.4f)
        Matrix.rotateM(modelMatrix, 0, 25f, 1f, 1f, 0f)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        colorProgram.use()
        colorProgram.setUniforms(mvpMatrix)
        cube.bindData(colorProgram)
        cube.draw()
    }

}
