package hr.foi.air.cinema.ui.admin

import com.google.firebase.Timestamp
import hr.foi.air.cinema.data.FakeScreeningRepository
import hr.foi.air.cinema.data.FakeTicketRepository
import hr.foi.air.cinema.data.Reservation
import hr.foi.air.cinema.data.ReservationStatus
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationRequestsViewModelTest {

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
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(),
            screeningRepository = FakeScreeningRepository(),
        )

        assertEquals(ReservationRequestsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun reservationsLoaded_areMatchedWithTheirScreeningAndSortedByNewestFirst() = runTest {
        val screening = Screening(id = "1", movieTitle = "Dune: Part Three")
        val older = Reservation(
            id = "res-1",
            screeningId = "1",
            userId = "user-1",
            status = ReservationStatus.PENDING,
            createdAt = Timestamp(1000, 0),
        )
        val newer = Reservation(
            id = "res-2",
            screeningId = "1",
            userId = "user-2",
            status = ReservationStatus.APPROVED,
            createdAt = Timestamp(2000, 0),
        )
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(reservationsFlow = flowOf(listOf(older, newer))),
            screeningRepository = FakeScreeningRepository(screeningsFlow = flowOf(listOf(screening))),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ReservationRequestsUiState.Success)
        val requests = (state as ReservationRequestsUiState.Success).requests
        assertEquals(listOf(newer, older), requests.map { it.reservation })
        assertEquals(screening, requests[0].screening)
        assertEquals(screening, requests[1].screening)
    }

    @Test
    fun reservationForMissingScreening_isReturnedWithNullScreening() = runTest {
        val reservation = Reservation(id = "res-1", screeningId = "deleted-screening", userId = "user-1")
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(reservationsFlow = flowOf(listOf(reservation))),
            screeningRepository = FakeScreeningRepository(screeningsFlow = flowOf(emptyList())),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ReservationRequestsUiState.Success)
        val requests = (state as ReservationRequestsUiState.Success).requests
        assertEquals(1, requests.size)
        assertNull(requests[0].screening)
    }

    @Test
    fun noReservations_updatesStateToSuccessWithEmptyList() = runTest {
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(reservationsFlow = flowOf(emptyList())),
            screeningRepository = FakeScreeningRepository(screeningsFlow = flowOf(emptyList())),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ReservationRequestsUiState.Success)
        assertTrue((state as ReservationRequestsUiState.Success).requests.isEmpty())
    }

    @Test
    fun repositoryError_updatesStateToError() = runTest {
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(
                reservationsFlow = flow { throw RuntimeException("Greška pri dohvaćanju zahtjeva za rezervaciju") },
            ),
            screeningRepository = FakeScreeningRepository(),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ReservationRequestsUiState.Error)
        assertEquals(
            "Greška pri dohvaćanju zahtjeva za rezervaciju",
            (state as ReservationRequestsUiState.Error).message,
        )
    }

    @Test
    fun initialApproveState_isIdle() {
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(),
            screeningRepository = FakeScreeningRepository(),
        )

        assertEquals(ApproveReservationUiState.Idle, viewModel.approveState.value)
    }

    @Test
    fun approveReservation_success_updatesStateToSuccessAndUpdatesStatusToApproved() = runTest {
        val ticketRepository = FakeTicketRepository()
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = ticketRepository,
            screeningRepository = FakeScreeningRepository(),
        )

        viewModel.approveReservation("res-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ApproveReservationUiState.Success, viewModel.approveState.value)
        assertEquals(1, ticketRepository.updateReservationStatusCallCount)
        assertEquals("res-1", ticketRepository.lastUpdatedReservationId)
        assertEquals(ReservationStatus.APPROVED, ticketRepository.lastUpdatedReservationStatus)
    }

    @Test
    fun approveReservation_setsInProgressStateForCorrectReservationId() {
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(),
            screeningRepository = FakeScreeningRepository(),
        )

        viewModel.approveReservation("res-42")

        val state = viewModel.approveState.value
        assertTrue(state is ApproveReservationUiState.InProgress)
        assertEquals("res-42", (state as ApproveReservationUiState.InProgress).reservationId)
    }

    @Test
    fun approveReservation_repositoryError_updatesStateToError() = runTest {
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(
                updateReservationStatusResult = Result.failure(RuntimeException("Greška pri odobravanju rezervacije")),
            ),
            screeningRepository = FakeScreeningRepository(),
        )

        viewModel.approveReservation("res-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.approveState.value
        assertTrue(state is ApproveReservationUiState.Error)
        assertEquals("Greška pri odobravanju rezervacije", (state as ApproveReservationUiState.Error).message)
    }

    @Test
    fun initialRejectState_isIdle() {
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(),
            screeningRepository = FakeScreeningRepository(),
        )

        assertEquals(RejectReservationUiState.Idle, viewModel.rejectState.value)
    }

    @Test
    fun rejectReservation_success_updatesStateToSuccessAndUpdatesStatusToRejected() = runTest {
        val ticketRepository = FakeTicketRepository()
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = ticketRepository,
            screeningRepository = FakeScreeningRepository(),
        )

        viewModel.rejectReservation("res-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(RejectReservationUiState.Success, viewModel.rejectState.value)
        assertEquals(1, ticketRepository.updateReservationStatusCallCount)
        assertEquals("res-1", ticketRepository.lastUpdatedReservationId)
        assertEquals(ReservationStatus.REJECTED, ticketRepository.lastUpdatedReservationStatus)
    }

    @Test
    fun rejectReservation_setsInProgressStateForCorrectReservationId() {
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(),
            screeningRepository = FakeScreeningRepository(),
        )

        viewModel.rejectReservation("res-42")

        val state = viewModel.rejectState.value
        assertTrue(state is RejectReservationUiState.InProgress)
        assertEquals("res-42", (state as RejectReservationUiState.InProgress).reservationId)
    }

    @Test
    fun rejectReservation_repositoryError_updatesStateToError() = runTest {
        val viewModel = ReservationRequestsViewModel(
            ticketRepository = FakeTicketRepository(
                updateReservationStatusResult = Result.failure(RuntimeException("Greška pri odbijanju rezervacije")),
            ),
            screeningRepository = FakeScreeningRepository(),
        )

        viewModel.rejectReservation("res-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.rejectState.value
        assertTrue(state is RejectReservationUiState.Error)
        assertEquals("Greška pri odbijanju rezervacije", (state as RejectReservationUiState.Error).message)
    }
}
