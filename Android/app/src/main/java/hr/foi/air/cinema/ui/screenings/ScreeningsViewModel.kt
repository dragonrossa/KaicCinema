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

enum class SortOption(val label: String) {
    NONE("Zadano"),
    MOST_VIEWED("Najgledaniji"),
    MOST_POPULAR("Najpopularniji"),
}

sealed interface ScreeningsUiState {
    data object Loading : ScreeningsUiState

    data class Success(
        val allScreenings: List<Screening>,
        val selectedCategory: String? = null,
        val sortOption: SortOption = SortOption.NONE,
    ) : ScreeningsUiState {
        val categories: List<String> get() = allScreenings.map { it.category }.distinct().sorted()

        val displayedScreenings: List<Screening> get() {
            val filtered = when (selectedCategory) {
                null -> allScreenings
                else -> allScreenings.filter { it.category == selectedCategory }
            }
            return when (sortOption) {
                SortOption.NONE -> filtered
                SortOption.MOST_VIEWED -> filtered.sortedByDescending { it.views }
                SortOption.MOST_POPULAR -> filtered.sortedByDescending { it.popularity }
            }
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
                    val previous = _uiState.value as? ScreeningsUiState.Success
                    _uiState.value = ScreeningsUiState.Success(
                        allScreenings = screenings,
                        selectedCategory = previous?.selectedCategory,
                        sortOption = previous?.sortOption ?: SortOption.NONE,
                    )
                }
        }
    }

    fun onCategorySelected(category: String?) {
        val current = _uiState.value
        if (current is ScreeningsUiState.Success) {
            _uiState.value = current.copy(selectedCategory = category)
        }
    }

    fun onSortOptionSelected(option: SortOption) {
        val current = _uiState.value
        if (current is ScreeningsUiState.Success) {
            _uiState.value = current.copy(sortOption = option)
        }
    }
}
