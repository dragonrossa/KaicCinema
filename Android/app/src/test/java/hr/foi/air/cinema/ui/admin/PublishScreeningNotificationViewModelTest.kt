package hr.foi.air.cinema.ui.admin

import hr.foi.air.cinema.data.FakeScreeningNotificationRepository
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
class PublishScreeningNotificationViewModelTest {

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
        val viewModel = PublishScreeningNotificationViewModel(
            screeningId = "screening-1",
            screeningNotificationRepository = FakeScreeningNotificationRepository(),
        )

        assertEquals(PublishScreeningNotificationUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun publishNotification_success_updatesStateToSuccessAndSavesToRepositoryForCorrectScreening() = runTest {
        val repository = FakeScreeningNotificationRepository()
        val viewModel = PublishScreeningNotificationViewModel(
            screeningId = "screening-1",
            screeningNotificationRepository = repository,
        )

        viewModel.publishNotification("Projekcija je pomaknuta na 20:00.")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PublishScreeningNotificationUiState.Success)
        assertEquals(1, repository.publishNotificationCallCount)
        assertEquals("screening-1", repository.lastScreeningId)
        assertEquals("Projekcija je pomaknuta na 20:00.", repository.lastMessage)
    }

    @Test
    fun publishNotification_blankMessage_updatesStateToErrorWithoutCallingRepository() {
        val repository = FakeScreeningNotificationRepository()
        val viewModel = PublishScreeningNotificationViewModel(
            screeningId = "screening-1",
            screeningNotificationRepository = repository,
        )

        viewModel.publishNotification("   ")

        assertTrue(viewModel.uiState.value is PublishScreeningNotificationUiState.Error)
        assertEquals(0, repository.publishNotificationCallCount)
    }

    @Test
    fun publishNotification_repositoryError_updatesStateToError() = runTest {
        val repository = FakeScreeningNotificationRepository(
            publishResult = { _, _ -> Result.failure(RuntimeException("Greška pri objavi obavijesti")) },
        )
        val viewModel = PublishScreeningNotificationViewModel(
            screeningId = "screening-1",
            screeningNotificationRepository = repository,
        )

        viewModel.publishNotification("Tekst obavijesti")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PublishScreeningNotificationUiState.Error)
        assertEquals("Greška pri objavi obavijesti", (state as PublishScreeningNotificationUiState.Error).message)
    }
}
