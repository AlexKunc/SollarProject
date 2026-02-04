package com.example.solaropengl.gl.phong

import android.opengl.GLES20
import com.example.solaropengl.gl.util.ShaderHelper

class PhongShaderProgram {

    val aPosition: Int
    val aNormal: Int

    private val uMVP: Int
    private val uModel: Int
    private val uNormalMat: Int

    private val uLightPos: Int
    private val uViewPos: Int

    private val uAmbient: Int
    private val uDiffuse: Int
    private val uSpecular: Int
    private val uShininess: Int

    private val programId: Int

    init {
        val vs = """
            uniform mat4 u_MVP;
            uniform mat4 u_Model;
            uniform mat3 u_NormalMat;

            attribute vec4 a_Position;
            attribute vec3 a_Normal;

            varying vec3 v_FragPos;
            varying vec3 v_Normal;

            void main() {
                v_FragPos = vec3(u_Model * a_Position);
                v_Normal = normalize(u_NormalMat * a_Normal);
                gl_Position = u_MVP * a_Position;
            }
        """.trimIndent()

        val fs = """
            precision mediump float;

            varying vec3 v_FragPos;
            varying vec3 v_Normal;

            uniform vec3 u_LightPos;
            uniform vec3 u_ViewPos;

            uniform vec3 u_Ambient;
            uniform vec3 u_Diffuse;
            uniform vec3 u_Specular;
            uniform float u_Shininess;

            void main() {
                vec3 norm = normalize(v_Normal);
                vec3 lightDir = normalize(u_LightPos - v_FragPos);

                // ambient
                vec3 ambient = u_Ambient;

                // diffuse
                float diff = max(dot(norm, lightDir), 0.0);
                vec3 diffuse = u_Diffuse * diff;

                // specular
                vec3 viewDir = normalize(u_ViewPos - v_FragPos);
                vec3 reflectDir = reflect(-lightDir, norm);
                float spec = pow(max(dot(viewDir, reflectDir), 0.0), u_Shininess);
                vec3 specular = u_Specular * spec;

                vec3 color = ambient + diffuse + specular;
                gl_FragColor = vec4(color, 1.0);
            }
        """.trimIndent()

        programId = ShaderHelper.buildProgram(vs, fs)

        aPosition = GLES20.glGetAttribLocation(programId, "a_Position")
        aNormal = GLES20.glGetAttribLocation(programId, "a_Normal")

        uMVP = GLES20.glGetUniformLocation(programId, "u_MVP")
        uModel = GLES20.glGetUniformLocation(programId, "u_Model")
        uNormalMat = GLES20.glGetUniformLocation(programId, "u_NormalMat")

        uLightPos = GLES20.glGetUniformLocation(programId, "u_LightPos")
        uViewPos = GLES20.glGetUniformLocation(programId, "u_ViewPos")

        uAmbient = GLES20.glGetUniformLocation(programId, "u_Ambient")
        uDiffuse = GLES20.glGetUniformLocation(programId, "u_Diffuse")
        uSpecular = GLES20.glGetUniformLocation(programId, "u_Specular")
        uShininess = GLES20.glGetUniformLocation(programId, "u_Shininess")
    }

    fun use() = GLES20.glUseProgram(programId)

    fun setMatrices(mvp: FloatArray, model: FloatArray, normalMat3: FloatArray) {
        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0)
        GLES20.glUniformMatrix4fv(uModel, 1, false, model, 0)
        GLES20.glUniformMatrix3fv(uNormalMat, 1, false, normalMat3, 0)
    }

    fun setLightAndMaterial(
        lightPos: FloatArray,
        viewPos: FloatArray,
        ambient: FloatArray,
        diffuse: FloatArray,
        specular: FloatArray,
        shininess: Float
    ) {
        GLES20.glUniform3f(uLightPos, lightPos[0], lightPos[1], lightPos[2])
        GLES20.glUniform3f(uViewPos, viewPos[0], viewPos[1], viewPos[2])

        GLES20.glUniform3f(uAmbient, ambient[0], ambient[1], ambient[2])
        GLES20.glUniform3f(uDiffuse, diffuse[0], diffuse[1], diffuse[2])
        GLES20.glUniform3f(uSpecular, specular[0], specular[1], specular[2])
        GLES20.glUniform1f(uShininess, shininess)
    }
}
