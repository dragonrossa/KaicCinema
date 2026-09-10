package hr.foi.air.cinema.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.FirestorePurchaseRepository
import hr.foi.air.cinema.data.FirestoreScreeningRepository
import hr.foi.air.cinema.data.FirestoreTicketRepository
import hr.foi.air.cinema.data.PurchaseRepository
import hr.foi.air.cinema.data.Screening
import hr.foi.air.cinema.data.ScreeningRepository
import hr.foi.air.cinema.data.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private const val MESSAGE_HAS_ACTIVE_BOOKINGS =
    "Projekcija ima aktivne rezervacije ili kupljene ulaznice i ne može se obrisati."

sealed interface ManageScreeningsUiState {
    data object Loading : ManageScreeningsUiState
    data class Success(val screenings: List<Screening>) : ManageScreeningsUiState
    data class Error(val message: String) : ManageScreeningsUiState
}

sealed interface DeleteScreeningUiState {
    data object Idle : DeleteScreeningUiState
    data class InProgress(val screeningId: String) : DeleteScreeningUiState
    data object Success : DeleteScreeningUiState
    data class Error(val message: String) : DeleteScreeningUiState
}

class ManageScreeningsViewModel(
    private val screeningRepository: ScreeningRepository = FirestoreScreeningRepository(),
    private val ticketRepository: TicketRepository = FirestoreTicketRepository(),
    private val purchaseRepository: PurchaseRepository = FirestorePurchaseRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManageScreeningsUiState>(ManageScreeningsUiState.Loading)
    val uiState: StateFlow<ManageScreeningsUiState> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteScreeningUiState>(DeleteScreeningUiState.Idle)
    val deleteState: StateFlow<DeleteScreeningUiState> = _deleteState.asStateFlow()

    init {
        viewModelScope.launch {
            screeningRepository.observeScreenings()
                .catch { error ->
                    _uiState.value = ManageScreeningsUiState.Error(error.message ?: "Greška pri dohvaćanju projekcija")
                }
                .collect { screenings -> _uiState.value = ManageScreeningsUiState.Success(screenings) }
        }
    }

    fun deleteScreening(screeningId: String) {
        _deleteState.value = DeleteScreeningUiState.InProgress(screeningId)
        viewModelScope.launch {
            val hasActiveReservation = ticketRepository.hasActiveReservationForScreening(screeningId)
            val hasPurchase = purchaseRepository.hasPurchaseForScreening(screeningId)

            val checkFailure = hasActiveReservation.exceptionOrNull() ?: hasPurchase.exceptionOrNull()
            if (checkFailure != null) {
                _deleteState.value = DeleteScreeningUiState.Error(
                    checkFailure.message ?: "Greška pri provjeri rezervacija i kupnji",
                )
                return@launch
            }

            if (hasActiveReservation.getOrDefault(false) || hasPurchase.getOrDefault(false)) {
                _deleteState.value = DeleteScreeningUiState.Error(MESSAGE_HAS_ACTIVE_BOOKINGS)
                return@launch
            }

            screeningRepository.deleteScreening(screeningId)
                .onSuccess { _deleteState.value = DeleteScreeningUiState.Success }
                .onFailure { error ->
                    _deleteState.value = DeleteScreeningUiState.Error(error.message ?: "Greška pri brisanju projekcije")
                }
        }
    }
}
