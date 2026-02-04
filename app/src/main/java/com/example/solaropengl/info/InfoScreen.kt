package com.example.solaropengl.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    bodyIndex: Int,
    onBack: () -> Unit
) {
    val title = if (bodyIndex == 8) {
        "Луна"
    } else {
        PlanetRepository.byId(bodyIndex)?.name ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (bodyIndex == 8) {
                OpenGLScreen(renderer = MoonPhongRenderer())
            } else {
                val planet = PlanetRepository.byId(bodyIndex)

                if (planet == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Нет данных для выбранного объекта")
                    }
                } else {
                    PlanetInfoContent(planet)
                }
            }
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
        Card(
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Image(
                painter = painterResource(id = planet.imageRes),
                contentDescription = planet.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = planet.description,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
