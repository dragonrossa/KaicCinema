package hr.foi.air.cinema.ui.screenings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import hr.foi.air.cinema.data.Screening
import hr.foi.air.cinema.ui.auth.LogoutButton
import hr.foi.air.cinema.ui.common.formatScreeningTime
import hr.foi.air.cinema.ui.theme.CinemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreeningsPage(
    onScreeningSelected: (String) -> Unit,
    onNewsClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScreeningsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Projekcije") },
                actions = {
                    TextButton(onClick = onNewsClick) {
                        Text("Vijesti")
                    }
                    LogoutButton(onLoggedOut = onLogout)
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (val state = uiState) {
                is ScreeningsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ScreeningsUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                is ScreeningsUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.categories.isNotEmpty()) {
                            CategoryFilterRow(
                                categories = state.categories,
                                selectedCategory = state.selectedCategory,
                                onCategorySelected = viewModel::onCategorySelected,
                            )
                        }
                        SortOptionRow(
                            selectedSortOption = state.sortOption,
                            onSortOptionSelected = viewModel::onSortOptionSelected,
                        )
                        ScreeningsList(
                            screenings = state.displayedScreenings,
                            onScreeningSelected = onScreeningSelected,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Sve") },
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortOptionRow(
    selectedSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        items(SortOption.entries) { option ->
            FilterChip(
                selected = selectedSortOption == option,
                onClick = { onSortOptionSelected(option) },
                label = { Text(option.label) },
            )
        }
    }
}

@Composable
private fun ScreeningsList(
    screenings: List<Screening>,
    onScreeningSelected: (String) -> Unit,
) {
    if (screenings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Trenutno nema projekcija",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        items(screenings, key = { it.id }) { screening ->
            ScreeningCard(
                screening = screening,
                onClick = { onScreeningSelected(screening.id) },
            )
        }
    }
}

@Composable
private fun ScreeningCard(screening: Screening, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = screening.movieTitle, style = MaterialTheme.typography.titleMedium)
            Text(text = formatScreeningTime(screening.screeningTime), style = MaterialTheme.typography.bodyMedium)
            Text(text = screening.category, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "views: ${screening.views}, popularity: ${screening.popularity}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreeningsPagePreview() {
    CinemaTheme {
        ScreeningsList(
            screenings = listOf(
                Screening(id = "1", movieTitle = "Dune: Part Three", screeningTime = Timestamp.now(), category = "3D"),
                Screening(id = "2", movieTitle = "Oppenheimer", screeningTime = Timestamp.now(), category = "Standard"),
            ),
            onScreeningSelected = {},
        )
    }
}
