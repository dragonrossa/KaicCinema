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
class ScreeningsViewModelTest {

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
        val viewModel = ScreeningsViewModel(FakeScreeningRepository())

        assertEquals(ScreeningsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun screeningsLoaded_updatesStateToSuccessWithScreenings() = runTest {
        val screenings = listOf(
            Screening(id = "1", movieTitle = "Dune: Part Three"),
            Screening(id = "2", movieTitle = "Oppenheimer"),
        )
        val viewModel = ScreeningsViewModel(
            repository = FakeScreeningRepository(screeningsFlow = flowOf(screenings)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ScreeningsUiState.Success)
        assertEquals(screenings, (state as ScreeningsUiState.Success).allScreenings)
    }

    @Test
    fun repositoryError_updatesStateToError() = runTest {
        val viewModel = ScreeningsViewModel(
            repository = FakeScreeningRepository(
                screeningsFlow = flow { throw RuntimeException("Greška pri dohvaćanju projekcija") },
            ),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ScreeningsUiState.Error)
        assertEquals("Greška pri dohvaćanju projekcija", (state as ScreeningsUiState.Error).message)
    }
}
