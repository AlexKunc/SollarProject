package com.example.solaropengl.gl.shaders

import android.opengl.GLES20
import com.example.solaropengl.gl.util.ShaderHelper

class TextureShaderProgram {

    val aPositionLocation: Int
    val aTexCoordLocation: Int
    private val uMvpMatrixLocation: Int
    private val uTextureUnitLocation: Int

    private val programId: Int

    init {
        val vertexShader = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            void main() {
                v_TexCoord = a_TexCoord;
                gl_Position = u_MVPMatrix * a_Position;
            }
        """.trimIndent()

        val fragmentShader = """
            precision mediump float;
            uniform sampler2D u_TextureUnit;
            varying vec2 v_TexCoord;
            void main() {
                gl_FragColor = texture2D(u_TextureUnit, v_TexCoord);
            }
        """.trimIndent()

        programId = ShaderHelper.buildProgram(vertexShader, fragmentShader)

        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        aTexCoordLocation = GLES20.glGetAttribLocation(programId, "a_TexCoord")
        uMvpMatrixLocation = GLES20.glGetUniformLocation(programId, "u_MVPMatrix")
        uTextureUnitLocation = GLES20.glGetUniformLocation(programId, "u_TextureUnit")
    }

    fun use() {
        GLES20.glUseProgram(programId)
    }

    fun setUniforms(mvpMatrix: FloatArray, textureId: Int) {
        GLES20.glUniformMatrix4fv(uMvpMatrixLocation, 1, false, mvpMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTextureUnitLocation, 0)
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
