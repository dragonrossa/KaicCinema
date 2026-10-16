package hr.foi.air.cinema.ui.auth

import androidx.lifecycle.ViewModel
import hr.foi.air.cinema.data.AnalyticsEvents
import hr.foi.air.cinema.data.AnalyticsLogger
import hr.foi.air.cinema.data.AuthRepository
import hr.foi.air.cinema.data.FirebaseAnalyticsLogger
import hr.foi.air.cinema.data.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface LogoutUiState {
    data object Idle : LogoutUiState
    data object LoggedOut : LogoutUiState
}

class LogoutViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
    private val analyticsLogger: AnalyticsLogger = FirebaseAnalyticsLogger(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<LogoutUiState>(LogoutUiState.Idle)
    val uiState: StateFlow<LogoutUiState> = _uiState.asStateFlow()

    fun logout() {
        authRepository.logout()
        analyticsLogger.logEvent(AnalyticsEvents.LOGOUT)
        _uiState.value = LogoutUiState.LoggedOut
    }
}
