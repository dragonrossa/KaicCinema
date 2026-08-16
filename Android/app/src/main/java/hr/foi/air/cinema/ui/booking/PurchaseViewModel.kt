package hr.foi.air.cinema.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.AuthRepository
import hr.foi.air.cinema.data.FirebaseAuthRepository
import hr.foi.air.cinema.data.FirestorePurchaseRepository
import hr.foi.air.cinema.data.Purchase
import hr.foi.air.cinema.data.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PurchaseUiState {
    data object Idle : PurchaseUiState
    data object InProgress : PurchaseUiState
    data class Success(val purchase: Purchase) : PurchaseUiState
    data class Error(val message: String) : PurchaseUiState
}

class PurchaseViewModel(
    private val screeningId: String,
    private val purchaseRepository: PurchaseRepository = FirestorePurchaseRepository(),
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<PurchaseUiState>(PurchaseUiState.Idle)
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    fun purchaseTicket() {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            _uiState.value = PurchaseUiState.Error("Niste prijavljeni")
            return
        }

        viewModelScope.launch {
            _uiState.value = PurchaseUiState.InProgress
            purchaseRepository.purchaseTicket(screeningId, userId)
                .onSuccess { purchase -> _uiState.value = PurchaseUiState.Success(purchase) }
                .onFailure { error -> _uiState.value = PurchaseUiState.Error(error.message ?: "Greška pri kupnji") }
        }
    }
}
