package com.example.solaropengl.gl.phong

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

class PhongSphere(stacks: Int = 32, slices: Int = 32) {

    private val interleavedBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val indexCount: Int

    // vertex: position(3) + normal(3)
    private val stride = (3 + 3) * 4

    init {
        val data = ArrayList<Float>() // interleaved
        val indices = ArrayList<Short>()

        for (i in 0..stacks) {
            val v = i.toFloat() / stacks
            val phi = Math.PI * v

            for (j in 0..slices) {
                val u = j.toFloat() / slices
                val theta = 2.0 * Math.PI * u

                val x = (sin(phi) * cos(theta)).toFloat()
                val y = cos(phi).toFloat()
                val z = (sin(phi) * sin(theta)).toFloat()

                // position
                data.add(x); data.add(y); data.add(z)
                // normal (для единичной сферы = position)
                data.add(x); data.add(y); data.add(z)
            }
        }

        val vertsPerRow = slices + 1
        for (i in 0 until stacks) {
            for (j in 0 until slices) {
                val a = (i * vertsPerRow + j).toShort()
                val b = ((i + 1) * vertsPerRow + j).toShort()
                val c = (i * vertsPerRow + (j + 1)).toShort()
                val d = ((i + 1) * vertsPerRow + (j + 1)).toShort()

                indices.add(a); indices.add(b); indices.add(c)
                indices.add(c); indices.add(b); indices.add(d)
            }
        }

        val vArr = data.toFloatArray()
        val iArr = ShortArray(indices.size) { indices[it] }

        interleavedBuffer = ByteBuffer.allocateDirect(vArr.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vArr)
            .apply { position(0) }

        indexBuffer = ByteBuffer.allocateDirect(iArr.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(iArr)
            .apply { position(0) }

        indexCount = iArr.size
    }

    fun bind(program: PhongShaderProgram) {
        interleavedBuffer.position(0)
        GLES20.glVertexAttribPointer(program.aPosition, 3, GLES20.GL_FLOAT, false, stride, interleavedBuffer)
        GLES20.glEnableVertexAttribArray(program.aPosition)

        interleavedBuffer.position(3)
        GLES20.glVertexAttribPointer(program.aNormal, 3, GLES20.GL_FLOAT, false, stride, interleavedBuffer)
        GLES20.glEnableVertexAttribArray(program.aNormal)

        interleavedBuffer.position(0)
    }

    fun draw() {
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
    }
}
