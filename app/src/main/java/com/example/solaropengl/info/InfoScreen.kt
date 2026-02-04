package com.example.solaropengl.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
            // Луна (Фонг)
            OpenGLScreen(renderer = MoonPhongRenderer())
        } else {
            // Планеты 0..7
            val planet = PlanetRepository.byId(bodyIndex)

            if (planet == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет данных для выбранного объекта")
                }
            } else {
                PlanetInfoContent(planet)
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

@Composable
private fun PlanetInfoContent(planet: PlanetInfo) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = planet.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Image(
                painter = painterResource(id = planet.imageRes),
                contentDescription = planet.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = planet.description,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
