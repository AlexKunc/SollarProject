package com.example.solaropengl.news

data class NewsTileState(
    val news: NewsItem,
    val likes: Int
)

data class NewsUiState(
    val tiles: List<NewsTileState>
)
