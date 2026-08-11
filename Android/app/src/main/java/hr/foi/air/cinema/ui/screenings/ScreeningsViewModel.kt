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

sealed interface ScreeningsUiState {
    data object Loading : ScreeningsUiState
    data class Success(val screenings: List<Screening>) : ScreeningsUiState
    data class Error(val message: String) : ScreeningsUiState
}

class ScreeningsViewModel(
    private val repository: ScreeningRepository = FirestoreScreeningRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScreeningsUiState>(ScreeningsUiState.Loading)
    val uiState: StateFlow<ScreeningsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeScreenings()
                .catch { error -> _uiState.value = ScreeningsUiState.Error(error.message ?: "Greška pri dohvaćanju projekcija") }
                .collect { screenings -> _uiState.value = ScreeningsUiState.Success(screenings) }
        }
    }
}
