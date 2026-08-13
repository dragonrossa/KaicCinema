package hr.foi.air.cinema.ui.auth

import hr.foi.air.cinema.data.FakeAuthRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class LogoutViewModelTest {

    @Test
    fun initialState_isIdle() {
        val viewModel = LogoutViewModel(FakeAuthRepository())

        assertEquals(LogoutUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun logout_callsRepositoryAndUpdatesStateToLoggedOut() {
        val authRepository = FakeAuthRepository()
        val viewModel = LogoutViewModel(authRepository)

        viewModel.logout()

        assertEquals(1, authRepository.logoutCallCount)
        assertEquals(LogoutUiState.LoggedOut, viewModel.uiState.value)
    }
}
