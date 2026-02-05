package com.example.solaropengl.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class NewsViewModel : ViewModel() {

    private val allNews: List<NewsItem> = listOf(
        NewsItem(1, "Галактические вести", "Астрономы обнаружили новую яркую туманность недалеко от созвездия Лебедя."),
        NewsItem(2, "Космическая погода", "На Солнце зафиксирована вспышка класса M. Возможны полярные сияния."),
        NewsItem(3, "Марс сегодня", "Ровер передал панораму кратера: видны слои породы и следы пылевых вихрей."),
        NewsItem(4, "Новости телескопов", "Новый снимок далёкой галактики показал необычную структуру спиральных рукавов."),
        NewsItem(5, "Орбитальная станция", "Экипаж завершил выход в открытый космос и установил новые панели."),
        NewsItem(6, "Лунная программа", "Сформирован план научных экспериментов для будущей базы у южного полюса."),
        NewsItem(7, "Экзопланеты", "Открыта планета в зоне обитаемости красного карлика. Идёт уточнение параметров."),
        NewsItem(8, "Кометы", "Новая комета получила временное обозначение и уже доступна для наблюдений в бинокль."),
        NewsItem(9, "Космические технологии", "Испытан новый двигатель для малых спутников — экономичнее на 15%."),
        NewsItem(10, "Метеорный поток", "На выходных ожидается пик активности потока. Лучшее время — перед рассветом.")
    )

    // лайки храним по id новости
    private val likesById = mutableMapOf<Int, Int>()

    private val _uiState = MutableStateFlow(
        NewsUiState(tiles = initialTiles())
    )
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        startAutoReplace()
    }

    fun like(tileIndex: Int) {
        _uiState.update { state ->
            val tiles = state.tiles.toMutableList()
            val tile = tiles[tileIndex]

            val id = tile.news.id
            val newLikes = (likesById[id] ?: tile.likes) + 1
            likesById[id] = newLikes

            tiles[tileIndex] = tile.copy(likes = newLikes)
            state.copy(tiles = tiles)
        }
    }

    private fun startAutoReplace() {
        viewModelScope.launch {
            while (isActive) {
                delay(5_000)

                _uiState.update { state ->
                    val tiles = state.tiles.toMutableList()

                    val replaceIndex = Random.nextInt(0, 4)
                    val currentIds = tiles.map { it.news.id }.toSet()

                    val candidates = allNews.filter { it.id !in currentIds }
                    val nextNews = if (candidates.isNotEmpty()) candidates.random() else allNews.random()

                    // подтяжка лайков
                    val savedLikes = likesById[nextNews.id] ?: 0
                    tiles[replaceIndex] = NewsTileState(news = nextNews, likes = savedLikes)

                    state.copy(tiles = tiles)
                }
            }
        }
    }

    private fun initialTiles(): List<NewsTileState> {
        val first = allNews.shuffled().take(4)
        return first.map { news ->
            NewsTileState(
                news = news,
                likes = likesById[news.id] ?: 0
            )
        }
    }
}
