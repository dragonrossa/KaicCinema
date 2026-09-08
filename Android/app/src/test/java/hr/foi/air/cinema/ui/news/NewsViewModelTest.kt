package hr.foi.air.cinema.ui.news

import hr.foi.air.cinema.data.FakeNewsRepository
import hr.foi.air.cinema.data.News
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading() {
        val viewModel = NewsViewModel(FakeNewsRepository())

        assertEquals(NewsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun newsLoaded_updatesStateToSuccessWithNews() = runTest {
        val news = listOf(
            News(id = "1", title = "Nova sezona filmova", content = "Uskoro stižu nove projekcije."),
            News(id = "2", title = "Popust za studente", content = "10% popusta uz studentsku iskaznicu."),
        )
        val viewModel = NewsViewModel(FakeNewsRepository(newsFlow = flowOf(news)))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is NewsUiState.Success)
        assertEquals(news, (state as NewsUiState.Success).news)
    }

    @Test
    fun repositoryError_updatesStateToError() = runTest {
        val viewModel = NewsViewModel(
            FakeNewsRepository(
                newsFlow = flow { throw RuntimeException("Greška pri dohvaćanju vijesti") },
            ),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is NewsUiState.Error)
        assertEquals("Greška pri dohvaćanju vijesti", (state as NewsUiState.Error).message)
    }
}
