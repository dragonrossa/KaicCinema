package hr.foi.air.cinema.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.AuthRepository
import hr.foi.air.cinema.data.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository(),
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
            _uiState.value = repository.login(email, password).fold(
                onSuccess = { LoginUiState.Success },
                onFailure = { error -> LoginUiState.Error(error.message ?: "Prijava nije uspjela") },
            )
        }
    }
}
