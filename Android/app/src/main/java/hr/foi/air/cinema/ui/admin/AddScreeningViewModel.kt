package hr.foi.air.cinema.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import hr.foi.air.cinema.data.FirestoreScreeningRepository
import hr.foi.air.cinema.data.Screening
import hr.foi.air.cinema.data.ScreeningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AddScreeningUiState {
    data object Idle : AddScreeningUiState
    data object InProgress : AddScreeningUiState
    data class Success(val screening: Screening) : AddScreeningUiState
    data class Error(val message: String) : AddScreeningUiState
}

class AddScreeningViewModel(
    private val screeningRepository: ScreeningRepository = FirestoreScreeningRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddScreeningUiState>(AddScreeningUiState.Idle)
    val uiState: StateFlow<AddScreeningUiState> = _uiState.asStateFlow()

    fun addScreening(
        movieTitle: String,
        description: String,
        category: String,
        totalSeatsInput: String,
        screeningTime: Timestamp,
    ) {
        if (movieTitle.isBlank()) {
            _uiState.value = AddScreeningUiState.Error("Unesite naziv filma")
            return
        }
        if (category.isBlank()) {
            _uiState.value = AddScreeningUiState.Error("Unesite kategoriju")
            return
        }
        val totalSeats = totalSeatsInput.toLongOrNull()
        if (totalSeats == null || totalSeats <= 0) {
            _uiState.value = AddScreeningUiState.Error("Unesite ispravan broj sjedala")
            return
        }

        val screening = Screening(
            movieTitle = movieTitle.trim(),
            description = description.trim(),
            screeningTime = screeningTime,
            category = category.trim(),
            totalSeats = totalSeats,
        )

        _uiState.value = AddScreeningUiState.InProgress
        viewModelScope.launch {
            screeningRepository.addScreening(screening)
                .onSuccess { saved -> _uiState.value = AddScreeningUiState.Success(saved) }
                .onFailure { error -> _uiState.value = AddScreeningUiState.Error(error.message ?: "Greška pri spremanju projekcije") }
        }
    }
}
