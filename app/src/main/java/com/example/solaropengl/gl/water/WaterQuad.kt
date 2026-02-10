package com.example.solaropengl.gl.water

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class WaterQuad {

    // X, Y, Z, U, V (на весь экран)
    private val data = floatArrayOf(
        -1f, -1f, 0f,   0f, 1f,
        1f, -1f, 0f,   1f, 1f,
        -1f,  1f, 0f,   0f, 0f,
        1f,  1f, 0f,   1f, 0f
    )

    private val vb: FloatBuffer = ByteBuffer.allocateDirect(data.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(data); position(0) }

    fun bindData(program: WaterShaderProgram) {
        vb.position(0)
        GLES20.glVertexAttribPointer(program.aPosition, 3, GLES20.GL_FLOAT, false, STRIDE, vb)
        GLES20.glEnableVertexAttribArray(program.aPosition)

        vb.position(3)
        GLES20.glVertexAttribPointer(program.aTexCoord, 2, GLES20.GL_FLOAT, false, STRIDE, vb)
        GLES20.glEnableVertexAttribArray(program.aTexCoord)

        vb.position(0)
    }

    fun draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    companion object {
        private const val STRIDE = (3 + 2) * 4
    }
}
