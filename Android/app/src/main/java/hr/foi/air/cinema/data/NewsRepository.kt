package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun observeNews(): Flow<List<News>>
    suspend fun addNews(news: News): Result<News>
}
