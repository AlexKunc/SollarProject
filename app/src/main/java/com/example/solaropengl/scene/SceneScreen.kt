package com.example.solaropengl.scene

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SceneScreen(
    onInfoClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SceneScreen (пока заглушка)")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onInfoClick) {
            Text("Информация")
        }
    }
}
