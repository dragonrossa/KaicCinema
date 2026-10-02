package hr.foi.air.cinema.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.foi.air.cinema.data.Screening

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularMoviesPage(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PopularMoviesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Popularni filmovi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag na admin panel")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Najgledaniji") },
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Najpopularniji") },
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is PopularMoviesUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is PopularMoviesUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                        )
                    }

                    is PopularMoviesUiState.Success -> {
                        val rankedScreenings = if (selectedTabIndex == 0) state.rankedByViews else state.rankedByPopularity
                        val countLabel = if (selectedTabIndex == 0) "Pregledi" else "Popularnost"
                        val countValue: (Screening) -> Long = if (selectedTabIndex == 0) {
                            { it.views }
                        } else {
                            { it.popularity }
                        }
                        RankedScreeningsList(
                            screenings = rankedScreenings,
                            countLabel = countLabel,
                            countValue = countValue,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankedScreeningsList(
    screenings: List<Screening>,
    countLabel: String,
    countValue: (Screening) -> Long,
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
        itemsIndexed(screenings, key = { _, screening -> screening.id }) { index, screening ->
            RankedScreeningRow(
                rank = index + 1,
                screening = screening,
                countLabel = countLabel,
                count = countValue(screening),
            )
        }
    }
}

@Composable
private fun RankedScreeningRow(
    rank: Int,
    screening: Screening,
    countLabel: String,
    count: Long,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "#$rank ${screening.movieTitle}", style = MaterialTheme.typography.titleMedium)
                Text(text = screening.category, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "$countLabel: $count",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
