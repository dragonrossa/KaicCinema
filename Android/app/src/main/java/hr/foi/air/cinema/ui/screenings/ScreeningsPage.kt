package hr.foi.air.cinema.ui.screenings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import hr.foi.air.cinema.ui.theme.CinemaTheme
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ScreeningsPage(
    modifier: Modifier = Modifier,
    viewModel: ScreeningsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
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
                ScreeningsList(screenings = state.screenings)
            }
        }
    }
}

@Composable
private fun ScreeningsList(screenings: List<Screening>) {
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
            ScreeningCard(screening = screening)
        }
    }
}

@Composable
private fun ScreeningCard(screening: Screening) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = screening.movieTitle, style = MaterialTheme.typography.titleMedium)
            Text(text = formatScreeningTime(screening.screeningTime), style = MaterialTheme.typography.bodyMedium)
            Text(text = screening.category, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatScreeningTime(timestamp: Timestamp): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.forLanguageTag("hr-HR"))
    return formatter.format(timestamp.toDate())
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
        )
    }
}
