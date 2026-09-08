package hr.foi.air.cinema.ui.admin

import hr.foi.air.cinema.data.FakeScreeningRepository
import hr.foi.air.cinema.data.Screening
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ManageScreeningsViewModelTest {

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
        val viewModel = ManageScreeningsViewModel(FakeScreeningRepository())

        assertEquals(ManageScreeningsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun screeningsLoaded_updatesStateToSuccessWithScreenings() = runTest {
        val screenings = listOf(
            Screening(id = "1", movieTitle = "Dune: Part Three"),
            Screening(id = "2", movieTitle = "Oppenheimer"),
        )
        val viewModel = ManageScreeningsViewModel(
            FakeScreeningRepository(screeningsFlow = flowOf(screenings)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ManageScreeningsUiState.Success)
        assertEquals(screenings, (state as ManageScreeningsUiState.Success).screenings)
    }

    @Test
    fun repositoryError_updatesStateToError() = runTest {
        val viewModel = ManageScreeningsViewModel(
            FakeScreeningRepository(
                screeningsFlow = kotlinx.coroutines.flow.flow { throw RuntimeException("Greška pri dohvaćanju projekcija") },
            ),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ManageScreeningsUiState.Error)
        assertEquals("Greška pri dohvaćanju projekcija", (state as ManageScreeningsUiState.Error).message)
    }

    @Test
    fun initialDeleteState_isIdle() {
        val viewModel = ManageScreeningsViewModel(FakeScreeningRepository())

        assertEquals(DeleteScreeningUiState.Idle, viewModel.deleteState.value)
    }

    @Test
    fun deleteScreening_success_updatesDeleteStateToSuccessAndCallsRepository() = runTest {
        val repository = FakeScreeningRepository()
        val viewModel = ManageScreeningsViewModel(repository)

        viewModel.deleteScreening("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DeleteScreeningUiState.Success, viewModel.deleteState.value)
        assertEquals(1, repository.deleteScreeningCallCount)
        assertEquals("1", repository.lastDeletedScreeningId)
    }

    @Test
    fun deleteScreening_setsInProgressStateForCorrectScreeningId() {
        val viewModel = ManageScreeningsViewModel(FakeScreeningRepository())

        viewModel.deleteScreening("42")

        val state = viewModel.deleteState.value
        assertTrue(state is DeleteScreeningUiState.InProgress)
        assertEquals("42", (state as DeleteScreeningUiState.InProgress).screeningId)
    }

    @Test
    fun deleteScreening_repositoryError_updatesDeleteStateToError() = runTest {
        val repository = FakeScreeningRepository(
            deleteScreeningResult = { Result.failure(RuntimeException("Greška pri brisanju projekcije")) },
        )
        val viewModel = ManageScreeningsViewModel(repository)

        viewModel.deleteScreening("1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.deleteState.value
        assertTrue(state is DeleteScreeningUiState.Error)
        assertEquals("Greška pri brisanju projekcije", (state as DeleteScreeningUiState.Error).message)
    }
}
