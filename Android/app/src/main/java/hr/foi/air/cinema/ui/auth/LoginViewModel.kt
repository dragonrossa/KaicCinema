package hr.foi.air.cinema.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.AuthRepository
import hr.foi.air.cinema.data.FirebaseAuthRepository
import hr.foi.air.cinema.data.FirestoreUserRepository
import hr.foi.air.cinema.data.UserRepository
import hr.foi.air.cinema.data.UserRole
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
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository(),
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

        return userRepository.getUserRole(uid).fold(
            onSuccess = { role -> LoginUiState.Success(role) },
            onFailure = { error -> LoginUiState.Error(error.message ?: "Greška pri dohvaćanju korisničke uloge") },
        )
    }
}
