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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface EditScreeningUiState {
    data object Loading : EditScreeningUiState
    data class Loaded(val screening: Screening) : EditScreeningUiState
    data object NotFound : EditScreeningUiState
    data class Error(val message: String) : EditScreeningUiState
}

sealed interface UpdateScreeningUiState {
    data object Idle : UpdateScreeningUiState
    data object InProgress : UpdateScreeningUiState
    data object Success : UpdateScreeningUiState
    data class Error(val message: String) : UpdateScreeningUiState
}

class EditScreeningViewModel(
    private val screeningId: String,
    private val screeningRepository: ScreeningRepository = FirestoreScreeningRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditScreeningUiState>(EditScreeningUiState.Loading)
    val uiState: StateFlow<EditScreeningUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateScreeningUiState>(UpdateScreeningUiState.Idle)
    val updateState: StateFlow<UpdateScreeningUiState> = _updateState.asStateFlow()

    private var loadedScreening: Screening? = null

    init {
        viewModelScope.launch {
            runCatching { screeningRepository.observeScreening(screeningId).first() }
                .onSuccess { screening ->
                    if (screening == null) {
                        _uiState.value = EditScreeningUiState.NotFound
                    } else {
                        loadedScreening = screening
                        _uiState.value = EditScreeningUiState.Loaded(screening)
                    }
                }
                .onFailure { error ->
                    _uiState.value = EditScreeningUiState.Error(error.message ?: "Greška pri dohvaćanju projekcije")
                }
        }
    }

    fun updateScreening(
        movieTitle: String,
        description: String,
        category: String,
        totalSeatsInput: String,
        screeningTime: Timestamp,
    ) {
        val current = loadedScreening
        if (current == null) {
            _updateState.value = UpdateScreeningUiState.Error("Projekcija nije učitana")
            return
        }
        if (movieTitle.isBlank()) {
            _updateState.value = UpdateScreeningUiState.Error("Unesite naziv filma")
            return
        }
        if (category.isBlank()) {
            _updateState.value = UpdateScreeningUiState.Error("Unesite kategoriju")
            return
        }
        val totalSeats = totalSeatsInput.toLongOrNull()
        if (totalSeats == null || totalSeats <= 0) {
            _updateState.value = UpdateScreeningUiState.Error("Unesite ispravan broj sjedala")
            return
        }
        if (totalSeats < current.reservedSeats) {
            _updateState.value = UpdateScreeningUiState.Error(
                "Broj sjedala ne može biti manji od već zauzetih (${current.reservedSeats})",
            )
            return
        }

        val updated = current.copy(
            movieTitle = movieTitle.trim(),
            description = description.trim(),
            category = category.trim(),
            totalSeats = totalSeats,
            screeningTime = screeningTime,
        )

        _updateState.value = UpdateScreeningUiState.InProgress
        viewModelScope.launch {
            screeningRepository.updateScreening(updated)
                .onSuccess {
                    loadedScreening = updated
                    _updateState.value = UpdateScreeningUiState.Success
                }
                .onFailure { error ->
                    _updateState.value = UpdateScreeningUiState.Error(error.message ?: "Greška pri spremanju izmjena")
                }
        }
    }
}
