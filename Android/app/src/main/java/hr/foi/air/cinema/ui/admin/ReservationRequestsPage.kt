package hr.foi.air.cinema.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.foi.air.cinema.data.ReservationStatus
import hr.foi.air.cinema.ui.common.formatScreeningTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationRequestsPage(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReservationRequestsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Zahtjevi za rezervaciju") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag na admin panel")
                    }
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
                is ReservationRequestsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ReservationRequestsUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                is ReservationRequestsUiState.Success -> {
                    ReservationRequestsList(requests = state.requests)
                }
            }
        }
    }
}

@Composable
private fun ReservationRequestsList(requests: List<ReservationRequest>) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Trenutno nema zahtjeva za rezervaciju",
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
        items(requests, key = { it.reservation.id }) { request -> ReservationRequestCard(request) }
    }
}

@Composable
private fun ReservationRequestCard(request: ReservationRequest) {
    val (statusLabel, statusColor) = when (request.reservation.status) {
        ReservationStatus.PENDING -> "Na čekanju" to MaterialTheme.colorScheme.tertiary
        ReservationStatus.APPROVED -> "Odobreno" to MaterialTheme.colorScheme.primary
        ReservationStatus.REJECTED -> "Odbijeno" to MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = request.screening?.movieTitle ?: "Nepoznata projekcija",
                style = MaterialTheme.typography.titleMedium,
            )
            if (request.screening != null) {
                Text(
                    text = formatScreeningTime(request.screening.screeningTime),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(text = "Korisnik: ${request.reservation.userId}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Zatraženo: ${formatScreeningTime(request.reservation.createdAt)}", style = MaterialTheme.typography.bodySmall)
            Text(text = statusLabel, color = statusColor, style = MaterialTheme.typography.labelLarge)
        }
    }
}
