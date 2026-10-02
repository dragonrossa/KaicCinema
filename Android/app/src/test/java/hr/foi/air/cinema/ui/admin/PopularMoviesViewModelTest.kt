package hr.foi.air.cinema.ui.admin

import hr.foi.air.cinema.data.FakeScreeningRepository
import hr.foi.air.cinema.data.Screening
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
class PopularMoviesViewModelTest {

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
        val viewModel = PopularMoviesViewModel(FakeScreeningRepository())

        assertEquals(PopularMoviesUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun screeningsLoaded_rankedByViewsDescending() = runTest {
        val leastViewed = Screening(id = "1", movieTitle = "Oppenheimer", views = 5, popularity = 50)
        val mostViewed = Screening(id = "2", movieTitle = "Dune: Part Three", views = 50, popularity = 5)
        val midViewed = Screening(id = "3", movieTitle = "Barbie", views = 20, popularity = 20)
        val viewModel = PopularMoviesViewModel(
            FakeScreeningRepository(screeningsFlow = flowOf(listOf(leastViewed, mostViewed, midViewed))),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PopularMoviesUiState.Success)
        assertEquals(
            listOf(mostViewed, midViewed, leastViewed),
            (state as PopularMoviesUiState.Success).rankedByViews,
        )
    }

    @Test
    fun screeningsLoaded_rankedByPopularityDescending() = runTest {
        val leastViewed = Screening(id = "1", movieTitle = "Oppenheimer", views = 5, popularity = 50)
        val mostViewed = Screening(id = "2", movieTitle = "Dune: Part Three", views = 50, popularity = 5)
        val midViewed = Screening(id = "3", movieTitle = "Barbie", views = 20, popularity = 20)
        val viewModel = PopularMoviesViewModel(
            FakeScreeningRepository(screeningsFlow = flowOf(listOf(leastViewed, mostViewed, midViewed))),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PopularMoviesUiState.Success)
        assertEquals(
            listOf(leastViewed, midViewed, mostViewed),
            (state as PopularMoviesUiState.Success).rankedByPopularity,
        )
    }

    @Test
    fun noScreenings_updatesStateToSuccessWithEmptyRankings() = runTest {
        val viewModel = PopularMoviesViewModel(
            FakeScreeningRepository(screeningsFlow = flowOf(emptyList())),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PopularMoviesUiState.Success)
        assertTrue((state as PopularMoviesUiState.Success).rankedByViews.isEmpty())
        assertTrue(state.rankedByPopularity.isEmpty())
    }

    @Test
    fun repositoryError_updatesStateToError() = runTest {
        val viewModel = PopularMoviesViewModel(
            FakeScreeningRepository(
                screeningsFlow = flow { throw RuntimeException("Greška pri dohvaćanju projekcija") },
            ),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PopularMoviesUiState.Error)
        assertEquals("Greška pri dohvaćanju projekcija", (state as PopularMoviesUiState.Error).message)
    }
}
