package com.example.solaropengl.news

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NewsScreen(
    onContinue: () -> Unit,
    vm: NewsViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // Верхняя панель
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Новости / реклама",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onContinue) {
                Text("Далее")
            }
        }

        // 4 четверти (2x2)
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NewsTile(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    tile = state.tiles[0],
                    onLike = { vm.like(0) }
                )
                NewsTile(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    tile = state.tiles[1],
                    onLike = { vm.like(1) }
                )
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NewsTile(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    tile = state.tiles[2],
                    onLike = { vm.like(2) }
                )
                NewsTile(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    tile = state.tiles[3],
                    onLike = { vm.like(3) }
                )
            }
        }
    }
}

@Composable
private fun NewsTile(
    modifier: Modifier,
    tile: NewsTileState,
    onLike: () -> Unit
) {
    Surface(
        modifier = modifier,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

            // 90% - новость
            Column(
                modifier = Modifier.weight(0.9f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(tile.news.title, fontWeight = FontWeight.SemiBold)
                Text(tile.news.body, style = MaterialTheme.typography.bodyMedium)
            }

            // 10% - лайки
            Row(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxWidth()
                    .clickable { onLike() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "❤",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tile.likes.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "лайк",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
