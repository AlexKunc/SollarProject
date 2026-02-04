package com.example.solaropengl.info

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solaropengl.gl.OpenGLScreen
import com.example.solaropengl.gl.phong.MoonPhongRenderer

@Composable
fun InfoScreen(
    bodyIndex: Int,
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {

        if (bodyIndex == 8) {
            // ЛУНА: OpenGL + Фонг
            OpenGLScreen(renderer = MoonPhongRenderer())
        } else {
            // Пока заглушка для планет (шаг 6 сделаем потом)
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Информация для планеты будет в шаге 6.\nСейчас Фонг реализован для Луны.")
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Text("Назад")
        }
    }
}
