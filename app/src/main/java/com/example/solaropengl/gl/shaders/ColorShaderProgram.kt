package com.example.solaropengl.gl.shaders

import android.opengl.GLES20
import com.example.solaropengl.gl.util.ShaderHelper

class ColorShaderProgram {

    val aPositionLocation: Int
    val aNormalLocation: Int

    private val uMvpMatrixLocation: Int
    private val uModelMatrixLocation: Int
    private val uColorLocation: Int
    private val uLightDirLocation: Int

    private val programId: Int

    init {
        val vertexShader = """
            uniform mat4 u_MVPMatrix;
            uniform mat4 u_ModelMatrix;

            attribute vec4 a_Position;
            attribute vec3 a_Normal;

            varying float v_Light;

            uniform vec3 u_LightDir; // в мировых координатах

            void main() {
                // позиция
                gl_Position = u_MVPMatrix * a_Position;

                // нормаль в мировое пространство (для сферы достаточно, но корректнее так)
                vec3 worldNormal = normalize((u_ModelMatrix * vec4(a_Normal, 0.0)).xyz);

                // Ламберт (диффуз)
                float diffuse = max(dot(worldNormal, normalize(u_LightDir)), 0.0);

                // небольшая "подсветка", чтобы не было совсем чёрных зон
                float ambient = 0.35;

                v_Light = ambient + diffuse * 0.75;
            }
        """.trimIndent()

        val fragmentShader = """
            precision mediump float;
            uniform vec4 u_Color;
            varying float v_Light;

            void main() {
                gl_FragColor = vec4(u_Color.rgb * v_Light, u_Color.a);
            }
        """.trimIndent()

        programId = ShaderHelper.buildProgram(vertexShader, fragmentShader)

        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        aNormalLocation = GLES20.glGetAttribLocation(programId, "a_Normal")

        uMvpMatrixLocation = GLES20.glGetUniformLocation(programId, "u_MVPMatrix")
        uModelMatrixLocation = GLES20.glGetUniformLocation(programId, "u_ModelMatrix")
        uColorLocation = GLES20.glGetUniformLocation(programId, "u_Color")
        uLightDirLocation = GLES20.glGetUniformLocation(programId, "u_LightDir")
    }

    fun use() {
        GLES20.glUseProgram(programId)
    }

    fun setUniforms(
        mvpMatrix: FloatArray,
        modelMatrix: FloatArray,
        r: Float, g: Float, b: Float, a: Float,
        lightDirX: Float, lightDirY: Float, lightDirZ: Float
    ) {
        GLES20.glUniformMatrix4fv(uMvpMatrixLocation, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0)

        GLES20.glUniform4f(uColorLocation, r, g, b, a)
        GLES20.glUniform3f(uLightDirLocation, lightDirX, lightDirY, lightDirZ)
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
