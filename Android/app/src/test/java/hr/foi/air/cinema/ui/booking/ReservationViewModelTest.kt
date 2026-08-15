package hr.foi.air.cinema.ui.booking

import hr.foi.air.cinema.data.FakeAuthRepository
import hr.foi.air.cinema.data.FakeTicketRepository
import hr.foi.air.cinema.data.Reservation
import hr.foi.air.cinema.data.ReservationStatus
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
class ReservationViewModelTest {

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
    fun initialState_isIdle() {
        val viewModel = ReservationViewModel(
            screeningId = "1",
            ticketRepository = FakeTicketRepository(),
            authRepository = FakeAuthRepository(),
        )

        assertEquals(ReservationUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun reserveTicket_success_updatesStateToSuccessWithPendingReservation() = runTest {
        val reservation = Reservation(id = "res-1", screeningId = "1", userId = "test-uid")
        val viewModel = ReservationViewModel(
            screeningId = "1",
            ticketRepository = FakeTicketRepository(reserveResult = Result.success(reservation)),
            authRepository = FakeAuthRepository(),
        )

        viewModel.reserveTicket()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ReservationUiState.Success)
        val success = state as ReservationUiState.Success
        assertEquals(reservation, success.reservation)
        assertEquals(ReservationStatus.PENDING, success.reservation.status)
    }

    @Test
    fun reserveTicket_noLoggedInUser_updatesStateToError() {
        val viewModel = ReservationViewModel(
            screeningId = "1",
            ticketRepository = FakeTicketRepository(),
            authRepository = FakeAuthRepository(userId = null),
        )

        viewModel.reserveTicket()

        assertTrue(viewModel.uiState.value is ReservationUiState.Error)
    }

    @Test
    fun reserveTicket_repositoryError_updatesStateToError() = runTest {
        val viewModel = ReservationViewModel(
            screeningId = "1",
            ticketRepository = FakeTicketRepository(
                reserveResult = Result.failure(RuntimeException("Greška pri rezervaciji")),
            ),
            authRepository = FakeAuthRepository(),
        )

        viewModel.reserveTicket()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ReservationUiState.Error)
        assertEquals("Greška pri rezervaciji", (state as ReservationUiState.Error).message)
    }
}
