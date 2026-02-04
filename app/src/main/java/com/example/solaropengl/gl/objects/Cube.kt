package com.example.solaropengl.gl.objects

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import com.example.solaropengl.gl.shaders.ColorShaderProgram

class Cube {

    // 8 вершин куба
    private val vertices = floatArrayOf(
        -1f, -1f, -1f,  // 0
        1f, -1f, -1f,  // 1
        1f,  1f, -1f,  // 2
        -1f,  1f, -1f,  // 3
        -1f, -1f,  1f,  // 4
        1f, -1f,  1f,  // 5
        1f,  1f,  1f,  // 6
        -1f,  1f,  1f   // 7
    )

    // 12 треугольников (36 индексов)
    private val indices = shortArrayOf(
        // back
        0, 1, 2,  0, 2, 3,
        // front
        4, 6, 5,  4, 7, 6,
        // left
        4, 0, 3,  4, 3, 7,
        // right
        1, 5, 6,  1, 6, 2,
        // bottom
        4, 5, 1,  4, 1, 0,
        // top
        3, 2, 6,  3, 6, 7
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertices)
            position(0)
        }

    private val indexBuffer: ShortBuffer = ByteBuffer
        .allocateDirect(indices.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .apply {
            put(indices)
            position(0)
        }

    fun bindData(program: ColorShaderProgram) {
        vertexBuffer.position(0)
        program.setVertexAttribPointer(
            buffer = vertexBuffer,
            attributeLocation = program.aPositionLocation,
            componentCount = 3,
            stride = 3 * 4
        )
    }


    fun draw() {
        android.opengl.GLES20.glDrawElements(
            android.opengl.GLES20.GL_TRIANGLES,
            indices.size,
            android.opengl.GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )
    }
}
