    package com.example.solaropengl.gl.objects

    import android.opengl.GLES20
    import com.example.solaropengl.gl.shaders.ColorShaderProgram
    import java.nio.ByteBuffer
    import java.nio.ByteOrder
    import java.nio.FloatBuffer
    import java.nio.ShortBuffer
    import kotlin.math.cos
    import kotlin.math.sin

    class Sphere(
        stacks: Int = 24,
        slices: Int = 24
    ) {
        private val vertexBuffer: FloatBuffer
        private val indexBuffer: ShortBuffer
        private val indexCount: Int

        // x,y,z,nx,ny,nz
        private val strideBytes = 6 * 4

        init {
            val vertices = ArrayList<Float>()
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
                    vertices.add(x); vertices.add(y); vertices.add(z)
                    // normal (для сферы = position, уже единичная)
                    vertices.add(x); vertices.add(y); vertices.add(z)
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

            val vArr = vertices.toFloatArray()
            val iArr = ShortArray(indices.size) { indices[it] }

            vertexBuffer = ByteBuffer.allocateDirect(vArr.size * 4)
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

        fun bindData(program: ColorShaderProgram) {
            // position
            vertexBuffer.position(0)
            GLES20.glVertexAttribPointer(program.aPositionLocation, 3, GLES20.GL_FLOAT, false, strideBytes, vertexBuffer)
            GLES20.glEnableVertexAttribArray(program.aPositionLocation)

            // normal (смещение 3 float)
            vertexBuffer.position(3)
            GLES20.glVertexAttribPointer(program.aNormalLocation, 3, GLES20.GL_FLOAT, false, strideBytes, vertexBuffer)
            GLES20.glEnableVertexAttribArray(program.aNormalLocation)
        }

        fun draw() {
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                indexCount,
                GLES20.GL_UNSIGNED_SHORT,
                indexBuffer
            )
        }
    }
