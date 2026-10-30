package hr.foi.air.cinema.logingoogle

import androidx.credentials.exceptions.GetCredentialCancellationException
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

class GoogleLoginViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val userRoleResolver: UserRoleResolver,
    private val authRepository: GoogleAuthRepository = FirebaseGoogleAuthRepository(),
    private val fcmTokenProvider: FcmTokenProvider = FirebaseFcmTokenProvider(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onSignInStarted() {
        _uiState.value = LoginUiState.Loading
    }

    fun onCredentialFetched(result: Result<String>) {
        result.fold(
            onSuccess = { idToken -> signInWithIdToken(idToken) },
            onFailure = { error -> _uiState.value = onCredentialFailure(error) },
        )
    }

    private fun signInWithIdToken(idToken: String) {
        viewModelScope.launch {
            _uiState.value = authRepository.signInWithIdToken(idToken).fold(
                onSuccess = { resolveRole() },
                onFailure = { error -> LoginUiState.Error(mapGoogleAuthErrorMessage(error)) },
            )
        }
    }

    private fun onCredentialFailure(error: Throwable): LoginUiState =
        if (error is GetCredentialCancellationException) {
            LoginUiState.Idle
        } else {
            LoginUiState.Error(mapGoogleAuthErrorMessage(error))
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
