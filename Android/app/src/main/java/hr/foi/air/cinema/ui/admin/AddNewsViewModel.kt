package hr.foi.air.cinema.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.FirestoreNewsRepository
import hr.foi.air.cinema.data.News
import hr.foi.air.cinema.data.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AddNewsUiState {
    data object Idle : AddNewsUiState
    data object InProgress : AddNewsUiState
    data class Success(val news: News) : AddNewsUiState
    data class Error(val message: String) : AddNewsUiState
}

class AddNewsViewModel(
    private val newsRepository: NewsRepository = FirestoreNewsRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddNewsUiState>(AddNewsUiState.Idle)
    val uiState: StateFlow<AddNewsUiState> = _uiState.asStateFlow()

    fun publishNews(title: String, content: String) {
        if (title.isBlank()) {
            _uiState.value = AddNewsUiState.Error("Unesite naslov")
            return
        }
        if (content.isBlank()) {
            _uiState.value = AddNewsUiState.Error("Unesite sadržaj")
            return
        }

        val news = News(title = title.trim(), content = content.trim())

        _uiState.value = AddNewsUiState.InProgress
        viewModelScope.launch {
            newsRepository.addNews(news)
                .onSuccess { saved -> _uiState.value = AddNewsUiState.Success(saved) }
                .onFailure { error -> _uiState.value = AddNewsUiState.Error(error.message ?: "Greška pri objavi vijesti") }
        }
    }
}
