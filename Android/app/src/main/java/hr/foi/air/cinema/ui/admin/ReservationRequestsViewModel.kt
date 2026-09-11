package hr.foi.air.cinema.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.FirestoreScreeningRepository
import hr.foi.air.cinema.data.FirestoreTicketRepository
import hr.foi.air.cinema.data.Reservation
import hr.foi.air.cinema.data.ReservationStatus
import hr.foi.air.cinema.data.Screening
import hr.foi.air.cinema.data.ScreeningRepository
import hr.foi.air.cinema.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ReservationRequest(
    val reservation: Reservation,
    val screening: Screening?,
)

sealed interface ReservationRequestsUiState {
    data object Loading : ReservationRequestsUiState
    data class Success(val requests: List<ReservationRequest>) : ReservationRequestsUiState
    data class Error(val message: String) : ReservationRequestsUiState
}

sealed interface ApproveReservationUiState {
    data object Idle : ApproveReservationUiState
    data class InProgress(val reservationId: String) : ApproveReservationUiState
    data object Success : ApproveReservationUiState
    data class Error(val message: String) : ApproveReservationUiState
}

class ReservationRequestsViewModel(
    private val ticketRepository: TicketRepository = FirestoreTicketRepository(),
    private val screeningRepository: ScreeningRepository = FirestoreScreeningRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReservationRequestsUiState>(ReservationRequestsUiState.Loading)
    val uiState: StateFlow<ReservationRequestsUiState> = _uiState.asStateFlow()

    private val _approveState = MutableStateFlow<ApproveReservationUiState>(ApproveReservationUiState.Idle)
    val approveState: StateFlow<ApproveReservationUiState> = _approveState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                ticketRepository.observeAllReservations(),
                screeningRepository.observeScreenings(),
            ) { reservations, screenings ->
                val screeningsById = screenings.associateBy { it.id }
                val requests = reservations
                    .sortedByDescending { it.createdAt }
                    .map { reservation -> ReservationRequest(reservation, screeningsById[reservation.screeningId]) }
                ReservationRequestsUiState.Success(requests) as ReservationRequestsUiState
            }
                .catch { error ->
                    emit(ReservationRequestsUiState.Error(error.message ?: "Greška pri dohvaćanju zahtjeva za rezervaciju"))
                }
                .collect { _uiState.value = it }
        }
    }

    fun approveReservation(reservationId: String) {
        _approveState.value = ApproveReservationUiState.InProgress(reservationId)
        viewModelScope.launch {
            ticketRepository.updateReservationStatus(reservationId, ReservationStatus.APPROVED)
                .onSuccess { _approveState.value = ApproveReservationUiState.Success }
                .onFailure { error ->
                    _approveState.value = ApproveReservationUiState.Error(
                        error.message ?: "Greška pri odobravanju rezervacije",
                    )
                }
        }
    }
}
