package hr.foi.air.cinema.ui.admin

import hr.foi.air.cinema.data.FakePurchaseRepository
import hr.foi.air.cinema.data.FakeTicketRepository
import hr.foi.air.cinema.data.Purchase
import hr.foi.air.cinema.data.Reservation
import hr.foi.air.cinema.data.ReservationStatus
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
class AdminBookingsViewModelTest {

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
        val viewModel = AdminBookingsViewModel(
            ticketRepository = FakeTicketRepository(),
            purchaseRepository = FakePurchaseRepository(),
        )

        assertEquals(AdminBookingsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun reservationsAndPurchasesLoaded_updatesStateToSuccess() = runTest {
        val reservation = Reservation(id = "res-1", screeningId = "1", userId = "user-1", status = ReservationStatus.PENDING)
        val purchase = Purchase(id = "purchase-1", screeningId = "2", userId = "user-2")
        val viewModel = AdminBookingsViewModel(
            ticketRepository = FakeTicketRepository(reservationsFlow = flowOf(listOf(reservation))),
            purchaseRepository = FakePurchaseRepository(purchasesFlow = flowOf(listOf(purchase))),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AdminBookingsUiState.Success)
        val success = state as AdminBookingsUiState.Success
        assertEquals(listOf(reservation), success.reservations)
        assertEquals(listOf(purchase), success.purchases)
    }

    @Test
    fun reservationsRepositoryError_updatesStateToError() = runTest {
        val viewModel = AdminBookingsViewModel(
            ticketRepository = FakeTicketRepository(
                reservationsFlow = flow { throw RuntimeException("Greška pri dohvaćanju podataka") },
            ),
            purchaseRepository = FakePurchaseRepository(),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AdminBookingsUiState.Error)
        assertEquals("Greška pri dohvaćanju podataka", (state as AdminBookingsUiState.Error).message)
    }
}
