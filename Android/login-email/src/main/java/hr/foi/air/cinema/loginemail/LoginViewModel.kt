package hr.foi.air.cinema.loginemail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.core.AnalyticsEvents
import hr.foi.air.cinema.core.AnalyticsLogger
import hr.foi.air.cinema.core.UserRole
import hr.foi.air.cinema.core.UserRoleResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val role: UserRole) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val userRoleResolver: UserRoleResolver,
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
    private val fcmTokenProvider: FcmTokenProvider = FirebaseFcmTokenProvider(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Unesite email i lozinku")
            return
        }

        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            _uiState.value = authRepository.login(email, password).fold(
                onSuccess = { resolveRole() },
                onFailure = { error -> LoginUiState.Error(error.message ?: "Prijava nije uspjela") },
            )
        }
    }

    private suspend fun resolveRole(): LoginUiState {
        val uid = authRepository.currentUserId()
            ?: return LoginUiState.Error("Prijava nije uspjela")

        return userRoleResolver.resolveRole(uid).fold(
            onSuccess = { role ->
                setUpPushNotifications(uid)
                analyticsLogger.logEvent(AnalyticsEvents.LOGIN)
                LoginUiState.Success(role)
            },
            onFailure = { error -> LoginUiState.Error(error.message ?: "Greška pri dohvaćanju korisničke uloge") },
        )
    }

    private suspend fun setUpPushNotifications(uid: String) {
        fcmTokenProvider.getToken().onSuccess { token -> fcmTokenProvider.saveToken(uid, token) }
        fcmTokenProvider.subscribeToNewScreenings()
        fcmTokenProvider.subscribeToScreeningNotifications()
    }
}
