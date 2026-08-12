package hr.foi.air.cinema.ui.screenings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.FirestoreScreeningRepository
import hr.foi.air.cinema.data.Screening
import hr.foi.air.cinema.data.ScreeningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface ScreeningDetailsUiState {
    data object Loading : ScreeningDetailsUiState
    data class Success(val screening: Screening) : ScreeningDetailsUiState
    data object NotFound : ScreeningDetailsUiState
    data class Error(val message: String) : ScreeningDetailsUiState
}

class ScreeningDetailsViewModel(
    private val screeningId: String,
    private val repository: ScreeningRepository = FirestoreScreeningRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScreeningDetailsUiState>(ScreeningDetailsUiState.Loading)
    val uiState: StateFlow<ScreeningDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeScreening(screeningId)
                .catch { error -> _uiState.value = ScreeningDetailsUiState.Error(error.message ?: "Greška pri dohvaćanju projekcije") }
                .collect { screening ->
                    _uiState.value = screening?.let { ScreeningDetailsUiState.Success(it) } ?: ScreeningDetailsUiState.NotFound
                }
        }
    }
}
