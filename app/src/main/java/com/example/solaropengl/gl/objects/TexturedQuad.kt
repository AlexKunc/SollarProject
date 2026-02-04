package com.example.solaropengl.gl.objects

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import com.example.solaropengl.gl.shaders.TextureShaderProgram

class TexturedQuad {

    // X, Y, Z, U, V
    private val data = floatArrayOf(
        -1f, -1f, 0f,   0f, 1f,
        1f, -1f, 0f,   1f, 1f,
        -1f,  1f, 0f,   0f, 0f,
        1f,  1f, 0f,   1f, 0f
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(data.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(data)
            position(0)
        }

    fun bindData(program: TextureShaderProgram) {
        vertexBuffer.position(0)
        program.setVertexAttribPointer(
            buffer = vertexBuffer,
            attributeLocation = program.aPositionLocation,
            componentCount = 3,
            stride = STRIDE
        )

        vertexBuffer.position(3)
        program.setVertexAttribPointer(
            buffer = vertexBuffer,
            attributeLocation = program.aTexCoordLocation,
            componentCount = 2,
            stride = STRIDE
        )

        vertexBuffer.position(0)
    }


    fun draw() {
        // Triangle strip: 4 вершины
        android.opengl.GLES20.glDrawArrays(android.opengl.GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    companion object {
        private const val STRIDE = (3 + 2) * 4
    }
}
