package hr.foi.air.cinema.ui.admin

import com.google.firebase.Timestamp
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
class EditScreeningViewModelTest {

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
    fun initialState_isLoading() {
        val viewModel = EditScreeningViewModel(
            screeningId = "1",
            screeningRepository = FakeScreeningRepository(),
        )

        assertEquals(EditScreeningUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun screeningFound_updatesStateToLoaded() = runTest {
        val screening = Screening(id = "1", movieTitle = "Dune: Part Three", totalSeats = 100)
        val viewModel = EditScreeningViewModel(
            screeningId = "1",
            screeningRepository = FakeScreeningRepository(screeningFlow = flowOf(screening)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is EditScreeningUiState.Loaded)
        assertEquals(screening, (state as EditScreeningUiState.Loaded).screening)
    }

    @Test
    fun screeningMissing_updatesStateToNotFound() = runTest {
        val viewModel = EditScreeningViewModel(
            screeningId = "missing",
            screeningRepository = FakeScreeningRepository(screeningFlow = flowOf(null)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(EditScreeningUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun repositoryError_updatesStateToError() = runTest {
        val viewModel = EditScreeningViewModel(
            screeningId = "1",
            screeningRepository = FakeScreeningRepository(
                screeningFlow = flow { throw RuntimeException("Greška pri dohvaćanju projekcije") },
            ),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is EditScreeningUiState.Error)
        assertEquals("Greška pri dohvaćanju projekcije", (state as EditScreeningUiState.Error).message)
    }

    @Test
    fun initialUpdateState_isIdle() {
        val viewModel = EditScreeningViewModel(
            screeningId = "1",
            screeningRepository = FakeScreeningRepository(),
        )

        assertEquals(UpdateScreeningUiState.Idle, viewModel.updateState.value)
    }

    @Test
    fun updateScreening_success_updatesStateToSuccessAndPreservesUnrelatedFields() = runTest {
        val screening = Screening(
            id = "1",
            movieTitle = "Dune: Part Three",
            category = "Sci-fi",
            totalSeats = 100,
            reservedSeats = 20,
            views = 50,
            popularity = 5,
        )
        val repository = FakeScreeningRepository(screeningFlow = flowOf(screening))
        val viewModel = EditScreeningViewModel(screeningId = "1", screeningRepository = repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateScreening(
            movieTitle = "Dune: Part Four",
            description = "Nastavak",
            category = "Drama",
            totalSeatsInput = "150",
            screeningTime = screeningTime,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UpdateScreeningUiState.Success, viewModel.updateState.value)
        val saved = repository.lastUpdatedScreening
        assertEquals("Dune: Part Four", saved?.movieTitle)
        assertEquals("Drama", saved?.category)
        assertEquals(150L, saved?.totalSeats)
        assertEquals(20L, saved?.reservedSeats)
        assertEquals(50L, saved?.views)
        assertEquals(5L, saved?.popularity)
        assertEquals("1", saved?.id)
    }

    @Test
    fun updateScreening_blankMovieTitle_updatesStateToErrorWithoutCallingRepository() = runTest {
        val screening = Screening(id = "1", movieTitle = "Dune: Part Three")
        val repository = FakeScreeningRepository(screeningFlow = flowOf(screening))
        val viewModel = EditScreeningViewModel(screeningId = "1", screeningRepository = repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateScreening(
            movieTitle = " ",
            description = "",
            category = "Drama",
            totalSeatsInput = "50",
            screeningTime = screeningTime,
        )

        assertTrue(viewModel.updateState.value is UpdateScreeningUiState.Error)
        assertEquals(0, repository.updateScreeningCallCount)
    }

    @Test
    fun updateScreening_totalSeatsBelowReservedSeats_updatesStateToErrorWithoutCallingRepository() = runTest {
        val screening = Screening(id = "1", movieTitle = "Dune: Part Three", reservedSeats = 30)
        val repository = FakeScreeningRepository(screeningFlow = flowOf(screening))
        val viewModel = EditScreeningViewModel(screeningId = "1", screeningRepository = repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateScreening(
            movieTitle = "Dune: Part Three",
            description = "",
            category = "Drama",
            totalSeatsInput = "10",
            screeningTime = screeningTime,
        )

        assertTrue(viewModel.updateState.value is UpdateScreeningUiState.Error)
        assertEquals(0, repository.updateScreeningCallCount)
    }

    @Test
    fun updateScreening_repositoryError_updatesStateToError() = runTest {
        val screening = Screening(id = "1", movieTitle = "Dune: Part Three")
        val repository = FakeScreeningRepository(
            screeningFlow = flowOf(screening),
            updateScreeningResult = { Result.failure(RuntimeException("Greška pri spremanju izmjena")) },
        )
        val viewModel = EditScreeningViewModel(screeningId = "1", screeningRepository = repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateScreening(
            movieTitle = "Dune: Part Three",
            description = "",
            category = "Drama",
            totalSeatsInput = "50",
            screeningTime = screeningTime,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.updateState.value
        assertTrue(state is UpdateScreeningUiState.Error)
        assertEquals("Greška pri spremanju izmjena", (state as UpdateScreeningUiState.Error).message)
    }
}
