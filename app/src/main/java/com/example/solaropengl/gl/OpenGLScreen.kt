package com.example.solaropengl.gl

import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.example.solaropengl.gl.water.NeptuneWaterRenderer

@Composable
fun OpenGLScreen(renderer: GLSurfaceView.Renderer) {
    AndroidView(factory = { context ->

        if (renderer is ContextAwareRenderer) {
            renderer.setContext(context)
        }

        val glView = GLSurfaceView(context).apply {
            setEGLContextClientVersion(2)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        glView.setOnTouchListener { _, event ->
            if (renderer is NeptuneWaterRenderer &&
                event.actionMasked == MotionEvent.ACTION_DOWN
            ) {
                val x = (event.x / glView.width.toFloat()).coerceIn(0f, 1f)
                val y = (event.y / glView.height.toFloat()).coerceIn(0f, 1f) // <-- без 1f-

                glView.queueEvent { renderer.onSplash(x, y) }
            }
            true
        }


        glView
    })
}
