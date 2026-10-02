package hr.foi.air.cinema.ui.admin

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

sealed interface PopularMoviesUiState {
    data object Loading : PopularMoviesUiState
    data class Success(val screenings: List<Screening>) : PopularMoviesUiState {
        val rankedByViews: List<Screening> get() = screenings.sortedByDescending { it.views }
        val rankedByPopularity: List<Screening> get() = screenings.sortedByDescending { it.popularity }
    }
    data class Error(val message: String) : PopularMoviesUiState
}

class PopularMoviesViewModel(
    private val screeningRepository: ScreeningRepository = FirestoreScreeningRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<PopularMoviesUiState>(PopularMoviesUiState.Loading)
    val uiState: StateFlow<PopularMoviesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            screeningRepository.observeScreenings()
                .catch { error -> _uiState.value = PopularMoviesUiState.Error(error.message ?: "Greška pri dohvaćanju projekcija") }
                .collect { screenings -> _uiState.value = PopularMoviesUiState.Success(screenings) }
        }
    }
}
