package com.example.solaropengl.gl

import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun OpenGLScreen(renderer: GLSurfaceView.Renderer) {
    AndroidView(factory = { context ->
        if (renderer is ContextAwareRenderer) renderer.setContext(context)

        GLSurfaceView(context).apply {
            setEGLContextClientVersion(2)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    })
}
