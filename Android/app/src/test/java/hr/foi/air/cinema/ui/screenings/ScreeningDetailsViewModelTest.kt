package hr.foi.air.cinema.ui.screenings

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
class ScreeningDetailsViewModelTest {

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
        val viewModel = ScreeningDetailsViewModel(
            screeningId = "1",
            repository = FakeScreeningRepository(),
        )

        assertEquals(ScreeningDetailsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun screeningFound_updatesStateToSuccessWithScreening() = runTest {
        val screening = Screening(id = "1", movieTitle = "Dune: Part Three")
        val viewModel = ScreeningDetailsViewModel(
            screeningId = "1",
            repository = FakeScreeningRepository(screeningFlow = flowOf(screening)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ScreeningDetailsUiState.Success)
        assertEquals(screening, (state as ScreeningDetailsUiState.Success).screening)
    }

    @Test
    fun screeningMissing_updatesStateToNotFound() = runTest {
        val viewModel = ScreeningDetailsViewModel(
            screeningId = "missing",
            repository = FakeScreeningRepository(screeningFlow = flowOf(null)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ScreeningDetailsUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun repositoryError_updatesStateToError() = runTest {
        val viewModel = ScreeningDetailsViewModel(
            screeningId = "1",
            repository = FakeScreeningRepository(
                screeningFlow = flow { throw RuntimeException("Greška pri dohvaćanju projekcije") },
            ),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ScreeningDetailsUiState.Error)
        assertEquals("Greška pri dohvaćanju projekcije", (state as ScreeningDetailsUiState.Error).message)
    }
}
