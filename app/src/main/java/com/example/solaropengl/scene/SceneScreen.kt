package com.example.solaropengl.scene

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solaropengl.gl.OpenGLScreen
import com.example.solaropengl.gl.SolarRenderer

@Composable
fun SceneScreen(onInfoClick: () -> Unit) {
    val renderer = androidx.compose.runtime.remember { SolarRenderer() }

    Box(Modifier.fillMaxSize()) {
        OpenGLScreen(renderer)
        Button(
            onClick = onInfoClick,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) { Text("Информация") }
    }
}

