package com.example.solaropengl.gl.water

import android.opengl.GLES20
import com.example.solaropengl.gl.util.ShaderHelper

class WaterShaderProgram {

    val aPosition: Int
    val aTexCoord: Int

    private val uMvp: Int
    private val uTime: Int
    private val uSplashPos: Int
    private val uSplashStart: Int

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

        val fs = """
            precision mediump float;

            varying vec2 v_UV;
            uniform float u_Time;

            uniform vec2 u_SplashPos;
            uniform float u_SplashStart;

            float wave(vec2 p, float dirx, float diry, float freq, float speed) {
                return sin((p.x*dirx + p.y*diry) * freq + u_Time * speed);
            }

            void main() {

                vec2 uv = v_UV;

                // ----- ТВОЯ АНИМАЦИЯ (НЕ ТРОГАЕМ) -----
                float w1 = wave(uv, 1.0, 0.2, 18.0, 1.2);
                float w2 = wave(uv, 0.2, 1.0, 12.0, 0.9);
                float w3 = wave(uv, -0.8, 0.6, 9.0, 0.7);

                float w = (w1*0.5 + w2*0.35 + w3*0.25);
                uv += vec2(w * 0.015, w * 0.010);

                float h =
                    0.55
                    + 0.22 * wave(uv, 1.0, 0.0, 26.0, 1.4)
                    + 0.18 * wave(uv, 0.0, 1.0, 19.0, 1.1)
                    + 0.10 * wave(uv, 0.7, 0.7, 13.0, 0.8);

                vec3 deep = vec3(0.02, 0.18, 0.32);
                vec3 shallow = vec3(0.08, 0.45, 0.65);
                vec3 col = mix(deep, shallow, clamp(h, 0.0, 1.0));

                float foam = smoothstep(0.78, 0.92, h);
                col += foam * 0.20;

                // ----- SPLASH -----
                float splashTime = u_Time - u_SplashStart;

                if (splashTime > 0.0 && splashTime < 2.0) {

                    float dist = distance(uv, u_SplashPos);

                    float ripple =
                        sin(35.0 * dist - splashTime * 10.0)
                        * exp(-5.0 * dist)
                        * exp(-1.4 * splashTime);

                    col += ripple * 0.25;
                }

                gl_FragColor = vec4(col, 1.0);
            }
        """.trimIndent()

        programId = ShaderHelper.buildProgram(vs, fs)

        aPosition = GLES20.glGetAttribLocation(programId, "a_Position")
        aTexCoord = GLES20.glGetAttribLocation(programId, "a_TexCoord")

        uMvp = GLES20.glGetUniformLocation(programId, "u_MVP")
        uTime = GLES20.glGetUniformLocation(programId, "u_Time")
        uSplashPos = GLES20.glGetUniformLocation(programId, "u_SplashPos")
        uSplashStart = GLES20.glGetUniformLocation(programId, "u_SplashStart")
    }

    fun use() = GLES20.glUseProgram(programId)

    fun setUniforms(
        mvpMatrix: FloatArray,
        timeSec: Float,
        splashX: Float,
        splashY: Float,
        splashStartTime: Float
    ) {
        GLES20.glUniformMatrix4fv(uMvp, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(uTime, timeSec)

        GLES20.glUniform2f(uSplashPos, splashX, splashY)
        GLES20.glUniform1f(uSplashStart, splashStartTime)
    }
}
