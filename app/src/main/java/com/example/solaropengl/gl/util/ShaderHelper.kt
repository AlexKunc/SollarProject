package com.example.solaropengl.gl.util

import android.opengl.GLES20

object ShaderHelper {

    fun buildProgram(vertexShaderSource: String, fragmentShaderSource: String): Int {
        val vertexShaderId = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShaderId = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource)

        val programId = GLES20.glCreateProgram()
        GLES20.glAttachShader(programId, vertexShaderId)
        GLES20.glAttachShader(programId, fragmentShaderId)
        GLES20.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val log = GLES20.glGetProgramInfoLog(programId)
            GLES20.glDeleteProgram(programId)
            throw RuntimeException("Program link failed: $log")
        }

        return programId
    }

    private fun compileShader(type: Int, shaderCode: String): Int {
        val shaderId = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shaderId, shaderCode)
        GLES20.glCompileShader(shaderId)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shaderId)
            GLES20.glDeleteShader(shaderId)
            throw RuntimeException("Shader compile failed: $log")
        }

        return shaderId
    }
}
