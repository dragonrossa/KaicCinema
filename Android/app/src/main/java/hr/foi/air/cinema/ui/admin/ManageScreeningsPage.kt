package hr.foi.air.cinema.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.foi.air.cinema.data.Screening
import hr.foi.air.cinema.ui.common.formatScreeningTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreeningsPage(
    onAddScreeningClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageScreeningsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    var screeningPendingDeletion by remember { mutableStateOf<Screening?>(null) }

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteScreeningUiState.Success) {
            screeningPendingDeletion = null
        }
    }

    screeningPendingDeletion?.let { screening ->
        AlertDialog(
            onDismissRequest = { screeningPendingDeletion = null },
            title = { Text("Obriši projekciju") },
            text = { Text("Jeste li sigurni da želite obrisati projekciju \"${screening.movieTitle}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteScreening(screening.id) }) {
                    Text("Obriši")
                }
            },
            dismissButton = {
                TextButton(onClick = { screeningPendingDeletion = null }) {
                    Text("Odustani")
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Upravljanje projekcijama") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag na admin panel")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddScreeningClick) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj projekciju")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (val state = uiState) {
                is ManageScreeningsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ManageScreeningsUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                is ManageScreeningsUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (deleteState is DeleteScreeningUiState.Error) {
                            Text(
                                text = (deleteState as DeleteScreeningUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                        ManageScreeningsList(
                            screenings = state.screenings,
                            deleteState = deleteState,
                            onDeleteClick = { screening -> screeningPendingDeletion = screening },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageScreeningsList(
    screenings: List<Screening>,
    deleteState: DeleteScreeningUiState,
    onDeleteClick: (Screening) -> Unit,
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
            val isDeleting = deleteState is DeleteScreeningUiState.InProgress && deleteState.screeningId == screening.id
            ManageScreeningRow(
                screening = screening,
                isDeleting = isDeleting,
                onDeleteClick = { onDeleteClick(screening) },
            )
        }
    }
}

@Composable
private fun ManageScreeningRow(
    screening: Screening,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = screening.movieTitle, style = MaterialTheme.typography.titleMedium)
                Text(text = formatScreeningTime(screening.screeningTime), style = MaterialTheme.typography.bodyMedium)
                Text(text = screening.category, style = MaterialTheme.typography.bodySmall)
            }
            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            } else {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Obriši projekciju")
                }
            }
        }
    }
}
