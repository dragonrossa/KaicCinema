package hr.foi.air.cinema.ui.auth

import hr.foi.air.cinema.data.AnalyticsEvents
import hr.foi.air.cinema.data.FakeAnalyticsLogger
import hr.foi.air.cinema.data.FakeAuthRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class LogoutViewModelTest {

    @Test
    fun initialState_isIdle() {
        val viewModel = LogoutViewModel(FakeAuthRepository(), FakeAnalyticsLogger())

        assertEquals(LogoutUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun logout_callsRepositoryAndUpdatesStateToLoggedOut() {
        val authRepository = FakeAuthRepository()
        val viewModel = LogoutViewModel(authRepository, FakeAnalyticsLogger())

        viewModel.logout()

        assertEquals(1, authRepository.logoutCallCount)
        assertEquals(LogoutUiState.LoggedOut, viewModel.uiState.value)
    }

    @Test
    fun logout_logsLogoutAnalyticsEvent() {
        val analyticsLogger = FakeAnalyticsLogger()
        val viewModel = LogoutViewModel(FakeAuthRepository(), analyticsLogger)

        viewModel.logout()

        assertEquals(1, analyticsLogger.loggedEvents.size)
        assertEquals(AnalyticsEvents.LOGOUT, analyticsLogger.loggedEvents.first().first)
    }
}
