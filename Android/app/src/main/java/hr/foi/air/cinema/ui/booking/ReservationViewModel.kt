package hr.foi.air.cinema.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.AuthRepository
import hr.foi.air.cinema.data.FirebaseAuthRepository
import hr.foi.air.cinema.data.FirestoreTicketRepository
import hr.foi.air.cinema.data.Reservation
import hr.foi.air.cinema.data.ReservationStatus
import hr.foi.air.cinema.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface ReservationUiState {
    data object Idle : ReservationUiState
    data object InProgress : ReservationUiState
    data class Success(val reservation: Reservation) : ReservationUiState
    data class Error(val message: String) : ReservationUiState
}

sealed interface ReservationStatusUiState {
    data object Loading : ReservationStatusUiState
    data object NoReservation : ReservationStatusUiState
    data class Active(val status: ReservationStatus) : ReservationStatusUiState
    data class Error(val message: String) : ReservationStatusUiState
}

class ReservationViewModel(
    private val screeningId: String,
    private val ticketRepository: TicketRepository = FirestoreTicketRepository(),
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val _reservationStatus = MutableStateFlow<ReservationStatusUiState>(ReservationStatusUiState.Loading)
    val reservationStatus: StateFlow<ReservationStatusUiState> = _reservationStatus.asStateFlow()

    init {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            _reservationStatus.value = ReservationStatusUiState.Error("Niste prijavljeni")
        } else {
            viewModelScope.launch {
                ticketRepository.observeReservation(screeningId, userId)
                    .catch { error ->
                        _reservationStatus.value = ReservationStatusUiState.Error(
                            error.message ?: "Greška pri dohvaćanju statusa rezervacije",
                        )
                    }
                    .collect { reservation ->
                        _reservationStatus.value = reservation?.let { ReservationStatusUiState.Active(it.status) }
                            ?: ReservationStatusUiState.NoReservation
                    }
            }
        }
    }

    fun reserveTicket() {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            _uiState.value = ReservationUiState.Error("Niste prijavljeni")
            return
        }

        viewModelScope.launch {
            _uiState.value = ReservationUiState.InProgress
            ticketRepository.reserveTicket(screeningId, userId)
                .onSuccess { reservation -> _uiState.value = ReservationUiState.Success(reservation) }
                .onFailure { error -> _uiState.value = ReservationUiState.Error(error.message ?: "Greška pri rezervaciji") }
        }
    }
}
