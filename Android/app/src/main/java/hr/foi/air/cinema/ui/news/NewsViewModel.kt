package hr.foi.air.cinema.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.cinema.data.FirestoreNewsRepository
import hr.foi.air.cinema.data.News
import hr.foi.air.cinema.data.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface NewsUiState {
    data object Loading : NewsUiState
    data class Success(val news: List<News>) : NewsUiState
    data class Error(val message: String) : NewsUiState
}

class NewsViewModel(
    private val newsRepository: NewsRepository = FirestoreNewsRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            newsRepository.observeNews()
                .catch { error -> _uiState.value = NewsUiState.Error(error.message ?: "Greška pri dohvaćanju vijesti") }
                .collect { news -> _uiState.value = NewsUiState.Success(news) }
        }
    }
}
