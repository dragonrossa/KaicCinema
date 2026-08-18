package hr.foi.air.cinema.ui.booking

import hr.foi.air.cinema.data.FakeAuthRepository
import hr.foi.air.cinema.data.FakePurchaseRepository
import hr.foi.air.cinema.data.Purchase
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
class PurchaseViewModelTest {

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
        val viewModel = PurchaseViewModel(
            screeningId = "1",
            purchaseRepository = FakePurchaseRepository(),
            authRepository = FakeAuthRepository(),
        )

        assertEquals(PurchaseUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun purchaseTicket_success_updatesStateToSuccessWithPurchase() = runTest {
        val purchase = Purchase(id = "purchase-1", screeningId = "1", userId = "test-uid")
        val viewModel = PurchaseViewModel(
            screeningId = "1",
            purchaseRepository = FakePurchaseRepository(purchaseResult = Result.success(purchase)),
            authRepository = FakeAuthRepository(),
        )

        viewModel.purchaseTicket()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PurchaseUiState.Success)
        assertEquals(purchase, (state as PurchaseUiState.Success).purchase)
    }

    @Test
    fun purchaseTicket_noLoggedInUser_updatesStateToError() {
        val viewModel = PurchaseViewModel(
            screeningId = "1",
            purchaseRepository = FakePurchaseRepository(),
            authRepository = FakeAuthRepository(userId = null),
        )

        viewModel.purchaseTicket()

        assertTrue(viewModel.uiState.value is PurchaseUiState.Error)
    }

    @Test
    fun purchaseTicket_succeeds_evenWithoutAnyReservation() = runTest {
        // SCRUM-120: reservation and purchase are an intentionally independent decision —
        // purchase never checks reservation status, so it must succeed with no reservation at all.
        val purchase = Purchase(id = "purchase-1", screeningId = "1", userId = "test-uid")
        val viewModel = PurchaseViewModel(
            screeningId = "1",
            purchaseRepository = FakePurchaseRepository(purchaseResult = Result.success(purchase)),
            authRepository = FakeAuthRepository(),
        )

        viewModel.purchaseTicket()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PurchaseUiState.Success)
    }

    @Test
    fun purchaseTicket_repositoryError_updatesStateToError() = runTest {
        val viewModel = PurchaseViewModel(
            screeningId = "1",
            purchaseRepository = FakePurchaseRepository(
                purchaseResult = Result.failure(RuntimeException("Greška pri kupnji")),
            ),
            authRepository = FakeAuthRepository(),
        )

        viewModel.purchaseTicket()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PurchaseUiState.Error)
        assertEquals("Greška pri kupnji", (state as PurchaseUiState.Error).message)
    }
}
