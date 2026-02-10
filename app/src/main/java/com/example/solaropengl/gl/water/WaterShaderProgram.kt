package com.example.solaropengl.gl.water

import android.opengl.GLES20
import com.example.solaropengl.gl.util.ShaderHelper

class WaterShaderProgram {

    val aPosition: Int
    val aTexCoord: Int

    private val uMvp: Int
    private val uTime: Int

    private val programId: Int

    init {
        val vs = """
            uniform mat4 u_MVP;
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_UV;

            void main() {
                v_UV = a_TexCoord;
                gl_Position = u_MVP * a_Position;
            }
        """.trimIndent()

        // Procedural water: простая сумма синус-волн + подсветка гребней
        val fs = """
            precision mediump float;

            varying vec2 v_UV;
            uniform float u_Time;

            float wave(vec2 p, float dirx, float diry, float freq, float speed) {
                return sin((p.x*dirx + p.y*diry) * freq + u_Time * speed);
            }

            void main() {
                vec2 uv = v_UV;

                // небольшая "дрожь" UV (искажение координат)
                float w1 = wave(uv, 1.0, 0.2, 18.0, 1.2);
                float w2 = wave(uv, 0.2, 1.0, 12.0, 0.9);
                float w3 = wave(uv, -0.8, 0.6, 9.0, 0.7);

                float w = (w1*0.5 + w2*0.35 + w3*0.25);
                uv += vec2(w * 0.015, w * 0.010);

                // “высота” волн
                float h =
                    0.55
                    + 0.22 * wave(uv, 1.0, 0.0, 26.0, 1.4)
                    + 0.18 * wave(uv, 0.0, 1.0, 19.0, 1.1)
                    + 0.10 * wave(uv, 0.7, 0.7, 13.0, 0.8);

                // базовый цвет воды (не задаю через uniform, чтобы было проще)
                vec3 deep = vec3(0.02, 0.18, 0.32);
                vec3 shallow = vec3(0.08, 0.45, 0.65);

                // смешиваем по “высоте”
                vec3 col = mix(deep, shallow, clamp(h, 0.0, 1.0));

                // подсветка гребней
                float foam = smoothstep(0.78, 0.92, h);
                col += foam * 0.20;

                gl_FragColor = vec4(col, 1.0);
            }
        """.trimIndent()

        programId = ShaderHelper.buildProgram(vs, fs)

        aPosition = GLES20.glGetAttribLocation(programId, "a_Position")
        aTexCoord = GLES20.glGetAttribLocation(programId, "a_TexCoord")

        uMvp = GLES20.glGetUniformLocation(programId, "u_MVP")
        uTime = GLES20.glGetUniformLocation(programId, "u_Time")
    }

    fun use() = GLES20.glUseProgram(programId)

    fun setUniforms(mvpMatrix: FloatArray, timeSec: Float) {
        GLES20.glUniformMatrix4fv(uMvp, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(uTime, timeSec)
    }
}
