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

    data class Success(
        val allScreenings: List<Screening>,
        val selectedCategory: String? = null,
    ) : ScreeningsUiState {
        val categories: List<String> get() = allScreenings.map { it.category }.distinct().sorted()

        val filteredScreenings: List<Screening> get() = when (selectedCategory) {
            null -> allScreenings
            else -> allScreenings.filter { it.category == selectedCategory }
        }
    }

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
                .collect { screenings ->
                    val selectedCategory = (_uiState.value as? ScreeningsUiState.Success)?.selectedCategory
                    _uiState.value = ScreeningsUiState.Success(allScreenings = screenings, selectedCategory = selectedCategory)
                }
        }
    }

    fun onCategorySelected(category: String?) {
        val current = _uiState.value
        if (current is ScreeningsUiState.Success) {
            _uiState.value = current.copy(selectedCategory = category)
        }
    }
}
