package com.example.solaropengl.scene

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solaropengl.gl.OpenGLScreen
import com.example.solaropengl.gl.SolarRenderer

@Composable
fun SceneScreen(
    onInfoClick: (Int) -> Unit
) {
    val renderer = remember { SolarRenderer() }
    var selected by remember { mutableIntStateOf(0) } // 0..8

    LaunchedEffect(selected) {
        renderer.setSelectedBodyIndex(selected)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OpenGLScreen(renderer = renderer)

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                selected = if (selected == 0) renderer.bodyCount - 1 else selected - 1
            }) { Text("Влево") }

            Button(onClick = {
                selected = (selected + 1) % renderer.bodyCount
            }) { Text("Вправо") }

            Button(onClick = { onInfoClick(selected) }) { Text("Информация") }
        }
    }
}
