package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeNewsRepository(
    private val newsFlow: Flow<List<News>> = flowOf(emptyList()),
    private val addNewsResult: (News) -> Result<News> = { Result.success(it) },
) : NewsRepository {

    var addNewsCallCount = 0
        private set
    var lastAddedNews: News? = null
        private set

    override fun observeNews(): Flow<List<News>> = newsFlow

    override suspend fun addNews(news: News): Result<News> {
        addNewsCallCount++
        lastAddedNews = news
        return addNewsResult(news)
    }
}
