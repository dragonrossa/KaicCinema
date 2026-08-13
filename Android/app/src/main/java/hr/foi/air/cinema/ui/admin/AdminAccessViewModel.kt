package hr.foi.air.cinema.ui.admin

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

sealed interface AdminAccessUiState {
    data object Checking : AdminAccessUiState
    data object Authorized : AdminAccessUiState
    data object Denied : AdminAccessUiState
}

class AdminAccessViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
    private val userRepository: UserRepository = FirestoreUserRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminAccessUiState>(AdminAccessUiState.Checking)
    val uiState: StateFlow<AdminAccessUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = authRepository.currentUserId()
            _uiState.value = if (uid == null) {
                AdminAccessUiState.Denied
            } else {
                userRepository.getUserRole(uid).fold(
                    onSuccess = { role -> if (role == UserRole.ADMIN) AdminAccessUiState.Authorized else AdminAccessUiState.Denied },
                    onFailure = { AdminAccessUiState.Denied },
                )
            }
        }
    }
}
