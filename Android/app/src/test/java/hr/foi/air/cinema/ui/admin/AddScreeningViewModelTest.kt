package hr.foi.air.cinema.ui.admin

import com.google.firebase.Timestamp
import hr.foi.air.cinema.data.FakeScreeningRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AddScreeningViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val screeningTime = Timestamp.now()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdle() {
        val viewModel = AddScreeningViewModel(FakeScreeningRepository())

        assertEquals(AddScreeningUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun addScreening_success_updatesStateToSuccessAndSavesToRepository() = runTest {
        val repository = FakeScreeningRepository()
        val viewModel = AddScreeningViewModel(repository)

        viewModel.addScreening(
            movieTitle = "Dune: Part Three",
            description = "Nastavak sage",
            category = "Znanstvena fantastika",
            totalSeatsInput = "120",
            screeningTime = screeningTime,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddScreeningUiState.Success)
        val saved = (state as AddScreeningUiState.Success).screening
        assertEquals("Dune: Part Three", saved.movieTitle)
        assertEquals("Znanstvena fantastika", saved.category)
        assertEquals(120L, saved.totalSeats)
        assertEquals(1, repository.addScreeningCallCount)
    }

    @Test
    fun addScreening_blankMovieTitle_updatesStateToErrorWithoutCallingRepository() {
        val repository = FakeScreeningRepository()
        val viewModel = AddScreeningViewModel(repository)

        viewModel.addScreening(
            movieTitle = "  ",
            description = "",
            category = "Drama",
            totalSeatsInput = "50",
            screeningTime = screeningTime,
        )

        assertTrue(viewModel.uiState.value is AddScreeningUiState.Error)
        assertEquals(0, repository.addScreeningCallCount)
    }

    @Test
    fun addScreening_blankCategory_updatesStateToErrorWithoutCallingRepository() {
        val repository = FakeScreeningRepository()
        val viewModel = AddScreeningViewModel(repository)

        viewModel.addScreening(
            movieTitle = "Dune: Part Three",
            description = "",
            category = " ",
            totalSeatsInput = "50",
            screeningTime = screeningTime,
        )

        assertTrue(viewModel.uiState.value is AddScreeningUiState.Error)
        assertEquals(0, repository.addScreeningCallCount)
    }

    @Test
    fun addScreening_invalidTotalSeats_updatesStateToErrorWithoutCallingRepository() {
        val repository = FakeScreeningRepository()
        val viewModel = AddScreeningViewModel(repository)

        viewModel.addScreening(
            movieTitle = "Dune: Part Three",
            description = "",
            category = "Drama",
            totalSeatsInput = "abc",
            screeningTime = screeningTime,
        )

        assertTrue(viewModel.uiState.value is AddScreeningUiState.Error)
        assertEquals(0, repository.addScreeningCallCount)
    }

    @Test
    fun addScreening_zeroTotalSeats_updatesStateToErrorWithoutCallingRepository() {
        val repository = FakeScreeningRepository()
        val viewModel = AddScreeningViewModel(repository)

        viewModel.addScreening(
            movieTitle = "Dune: Part Three",
            description = "",
            category = "Drama",
            totalSeatsInput = "0",
            screeningTime = screeningTime,
        )

        assertTrue(viewModel.uiState.value is AddScreeningUiState.Error)
        assertEquals(0, repository.addScreeningCallCount)
    }

    @Test
    fun addScreening_repositoryError_updatesStateToError() = runTest {
        val repository = FakeScreeningRepository(
            addScreeningResult = { Result.failure(RuntimeException("Greška pri spremanju projekcije")) },
        )
        val viewModel = AddScreeningViewModel(repository)

        viewModel.addScreening(
            movieTitle = "Dune: Part Three",
            description = "",
            category = "Drama",
            totalSeatsInput = "50",
            screeningTime = screeningTime,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddScreeningUiState.Error)
        assertEquals("Greška pri spremanju projekcije", (state as AddScreeningUiState.Error).message)
    }
}
