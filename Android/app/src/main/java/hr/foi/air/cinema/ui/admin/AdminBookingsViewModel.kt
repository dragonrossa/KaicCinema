package hr.foi.air.cinema.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.FirestorePurchaseRepository
import hr.foi.air.cinema.data.FirestoreTicketRepository
import hr.foi.air.cinema.data.Purchase
import hr.foi.air.cinema.data.PurchaseRepository
import hr.foi.air.cinema.data.Reservation
import hr.foi.air.cinema.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface AdminBookingsUiState {
    data object Loading : AdminBookingsUiState
    data class Success(val reservations: List<Reservation>, val purchases: List<Purchase>) : AdminBookingsUiState
    data class Error(val message: String) : AdminBookingsUiState
}

class AdminBookingsViewModel(
    private val ticketRepository: TicketRepository = FirestoreTicketRepository(),
    private val purchaseRepository: PurchaseRepository = FirestorePurchaseRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminBookingsUiState>(AdminBookingsUiState.Loading)
    val uiState: StateFlow<AdminBookingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                ticketRepository.observeAllReservations(),
                purchaseRepository.observeAllPurchases(),
            ) { reservations, purchases -> AdminBookingsUiState.Success(reservations, purchases) as AdminBookingsUiState }
                .catch { error ->
                    emit(AdminBookingsUiState.Error(error.message ?: "Greška pri dohvaćanju podataka"))
                }
                .collect { _uiState.value = it }
        }
    }
}
