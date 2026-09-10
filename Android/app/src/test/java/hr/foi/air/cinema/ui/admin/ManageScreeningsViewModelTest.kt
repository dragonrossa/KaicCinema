package hr.foi.air.cinema.ui.admin

import hr.foi.air.cinema.data.FakePurchaseRepository
import hr.foi.air.cinema.data.FakeScreeningRepository
import hr.foi.air.cinema.data.FakeTicketRepository
import hr.foi.air.cinema.data.Screening
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = FakeScreeningRepository(),
            ticketRepository = FakeTicketRepository(),
            purchaseRepository = FakePurchaseRepository(),
        )

        assertEquals(ManageScreeningsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun screeningsLoaded_updatesStateToSuccessWithScreenings() = runTest {
        val screenings = listOf(
            Screening(id = "1", movieTitle = "Dune: Part Three"),
            Screening(id = "2", movieTitle = "Oppenheimer"),
        )
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = FakeScreeningRepository(screeningsFlow = flowOf(screenings)),
            ticketRepository = FakeTicketRepository(),
            purchaseRepository = FakePurchaseRepository(),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ManageScreeningsUiState.Success)
        assertEquals(screenings, (state as ManageScreeningsUiState.Success).screenings)
    }

    @Test
    fun screeningsList_reflectsAdditionsAndDeletionsAsRepositoryEmitsNewSnapshots() = runTest {
        val dune = Screening(id = "1", movieTitle = "Dune: Part Three")
        val oppenheimer = Screening(id = "2", movieTitle = "Oppenheimer")
        val screeningsFlow = MutableStateFlow(listOf(dune))
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = FakeScreeningRepository(screeningsFlow = screeningsFlow),
            ticketRepository = FakeTicketRepository(),
            purchaseRepository = FakePurchaseRepository(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(dune), (viewModel.uiState.value as ManageScreeningsUiState.Success).screenings)

        screeningsFlow.value = listOf(dune, oppenheimer)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(dune, oppenheimer),
            (viewModel.uiState.value as ManageScreeningsUiState.Success).screenings,
        )

        screeningsFlow.value = listOf(oppenheimer)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(oppenheimer), (viewModel.uiState.value as ManageScreeningsUiState.Success).screenings)
    }

    @Test
    fun repositoryError_updatesStateToError() = runTest {
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = FakeScreeningRepository(
                screeningsFlow = kotlinx.coroutines.flow.flow { throw RuntimeException("Greška pri dohvaćanju projekcija") },
            ),
            ticketRepository = FakeTicketRepository(),
            purchaseRepository = FakePurchaseRepository(),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ManageScreeningsUiState.Error)
        assertEquals("Greška pri dohvaćanju projekcija", (state as ManageScreeningsUiState.Error).message)
    }

    @Test
    fun initialDeleteState_isIdle() {
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = FakeScreeningRepository(),
            ticketRepository = FakeTicketRepository(),
            purchaseRepository = FakePurchaseRepository(),
        )

        assertEquals(DeleteScreeningUiState.Idle, viewModel.deleteState.value)
    }

    @Test
    fun deleteScreening_noActiveBookings_updatesDeleteStateToSuccessAndCallsRepository() = runTest {
        val repository = FakeScreeningRepository()
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = repository,
            ticketRepository = FakeTicketRepository(hasActiveReservationResult = Result.success(false)),
            purchaseRepository = FakePurchaseRepository(hasPurchaseResult = Result.success(false)),
        )

        viewModel.deleteScreening("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DeleteScreeningUiState.Success, viewModel.deleteState.value)
        assertEquals(1, repository.deleteScreeningCallCount)
        assertEquals("1", repository.lastDeletedScreeningId)
    }

    @Test
    fun deleteScreening_setsInProgressStateForCorrectScreeningId() {
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = FakeScreeningRepository(),
            ticketRepository = FakeTicketRepository(),
            purchaseRepository = FakePurchaseRepository(),
        )

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
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = repository,
            ticketRepository = FakeTicketRepository(hasActiveReservationResult = Result.success(false)),
            purchaseRepository = FakePurchaseRepository(hasPurchaseResult = Result.success(false)),
        )

        viewModel.deleteScreening("1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.deleteState.value
        assertTrue(state is DeleteScreeningUiState.Error)
        assertEquals("Greška pri brisanju projekcije", (state as DeleteScreeningUiState.Error).message)
    }

    @Test
    fun deleteScreening_hasActiveReservation_blocksDeletionAndDoesNotCallScreeningRepository() = runTest {
        val repository = FakeScreeningRepository()
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = repository,
            ticketRepository = FakeTicketRepository(hasActiveReservationResult = Result.success(true)),
            purchaseRepository = FakePurchaseRepository(hasPurchaseResult = Result.success(false)),
        )

        viewModel.deleteScreening("1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.deleteState.value
        assertTrue(state is DeleteScreeningUiState.Error)
        assertEquals(0, repository.deleteScreeningCallCount)
    }

    @Test
    fun deleteScreening_hasPurchase_blocksDeletionAndDoesNotCallScreeningRepository() = runTest {
        val repository = FakeScreeningRepository()
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = repository,
            ticketRepository = FakeTicketRepository(hasActiveReservationResult = Result.success(false)),
            purchaseRepository = FakePurchaseRepository(hasPurchaseResult = Result.success(true)),
        )

        viewModel.deleteScreening("1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.deleteState.value
        assertTrue(state is DeleteScreeningUiState.Error)
        assertEquals(0, repository.deleteScreeningCallCount)
    }

    @Test
    fun deleteScreening_bookingCheckFails_updatesDeleteStateToErrorWithoutCallingScreeningRepository() = runTest {
        val repository = FakeScreeningRepository()
        val viewModel = ManageScreeningsViewModel(
            screeningRepository = repository,
            ticketRepository = FakeTicketRepository(
                hasActiveReservationResult = Result.failure(RuntimeException("Greška pri provjeri rezervacija")),
            ),
            purchaseRepository = FakePurchaseRepository(hasPurchaseResult = Result.success(false)),
        )

        viewModel.deleteScreening("1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.deleteState.value
        assertTrue(state is DeleteScreeningUiState.Error)
        assertEquals("Greška pri provjeri rezervacija", (state as DeleteScreeningUiState.Error).message)
        assertEquals(0, repository.deleteScreeningCallCount)
    }
}
