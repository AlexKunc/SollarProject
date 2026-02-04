package com.example.solaropengl.gl.shaders

import android.opengl.GLES20
import com.example.solaropengl.gl.util.ShaderHelper

class ColorShaderProgram {

    val aPositionLocation: Int
    private val uMvpMatrixLocation: Int
    private val uColorLocation: Int

    private val programId: Int

    init {
        val vertexShader = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
            }
        """.trimIndent()

        val fragmentShader = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
                gl_FragColor = u_Color;
            }
        """.trimIndent()

        programId = ShaderHelper.buildProgram(vertexShader, fragmentShader)

        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        uMvpMatrixLocation = GLES20.glGetUniformLocation(programId, "u_MVPMatrix")
        uColorLocation = GLES20.glGetUniformLocation(programId, "u_Color")
    }

    fun use() {
        GLES20.glUseProgram(programId)
    }

    fun setUniforms(mvpMatrix: FloatArray) {
        GLES20.glUniformMatrix4fv(uMvpMatrixLocation, 1, false, mvpMatrix, 0)
        // цвет куба
        GLES20.glUniform4f(uColorLocation, 0.2f, 0.8f, 1.0f, 1.0f)
    }

    fun setVertexAttribPointer(
        buffer: java.nio.FloatBuffer,
        attributeLocation: Int,
        componentCount: Int,
        stride: Int
    ) {
        GLES20.glVertexAttribPointer(
            attributeLocation,
            componentCount,
            GLES20.GL_FLOAT,
            false,
            stride,
            buffer
        )
        GLES20.glEnableVertexAttribArray(attributeLocation)
    }

}
