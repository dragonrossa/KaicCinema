package hr.foi.air.cinema.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.FirestoreScreeningNotificationRepository
import hr.foi.air.cinema.data.ScreeningNotification
import hr.foi.air.cinema.data.ScreeningNotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PublishScreeningNotificationUiState {
    data object Idle : PublishScreeningNotificationUiState
    data object InProgress : PublishScreeningNotificationUiState
    data class Success(val notification: ScreeningNotification) : PublishScreeningNotificationUiState
    data class Error(val message: String) : PublishScreeningNotificationUiState
}

class PublishScreeningNotificationViewModel(
    private val screeningId: String,
    private val screeningNotificationRepository: ScreeningNotificationRepository = FirestoreScreeningNotificationRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<PublishScreeningNotificationUiState>(PublishScreeningNotificationUiState.Idle)
    val uiState: StateFlow<PublishScreeningNotificationUiState> = _uiState.asStateFlow()

    fun publishNotification(message: String) {
        if (message.isBlank()) {
            _uiState.value = PublishScreeningNotificationUiState.Error("Unesite tekst obavijesti")
            return
        }

        _uiState.value = PublishScreeningNotificationUiState.InProgress
        viewModelScope.launch {
            screeningNotificationRepository.publishNotification(screeningId, message.trim())
                .onSuccess { notification -> _uiState.value = PublishScreeningNotificationUiState.Success(notification) }
                .onFailure { error ->
                    _uiState.value = PublishScreeningNotificationUiState.Error(
                        error.message ?: "Greška pri objavi obavijesti",
                    )
                }
        }
    }
}
