package hr.foi.air.cinema.ui.screenings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hr.foi.air.cinema.data.ReservationStatus
import hr.foi.air.cinema.data.Screening
import hr.foi.air.cinema.ui.booking.PurchaseUiState
import hr.foi.air.cinema.ui.booking.PurchaseViewModel
import hr.foi.air.cinema.ui.booking.ReservationStatusUiState
import hr.foi.air.cinema.ui.booking.ReservationUiState
import hr.foi.air.cinema.ui.booking.ReservationViewModel
import hr.foi.air.cinema.ui.common.formatScreeningTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreeningDetailsPage(
    screeningId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScreeningDetailsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ScreeningDetailsViewModel(screeningId = screeningId) }
        },
    ),
    reservationViewModel: ReservationViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ReservationViewModel(screeningId = screeningId) }
        },
    ),
    purchaseViewModel: PurchaseViewModel = viewModel(
        factory = viewModelFactory {
            initializer { PurchaseViewModel(screeningId = screeningId) }
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsState()
    val reservationState by reservationViewModel.uiState.collectAsState()
    val reservationStatus by reservationViewModel.reservationStatus.collectAsState()
    val purchaseState by purchaseViewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalji projekcije") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag na popis")
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
                is ScreeningDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ScreeningDetailsUiState.NotFound -> {
                    Text(
                        text = "Projekcija nije pronađena",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                is ScreeningDetailsUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                is ScreeningDetailsUiState.Success -> {
                    ScreeningDetailsContent(
                        screening = state.screening,
                        reservationState = reservationState,
                        onReserveClick = reservationViewModel::reserveTicket,
                        reservationStatus = reservationStatus,
                        purchaseState = purchaseState,
                        onPurchaseClick = purchaseViewModel::purchaseTicket,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreeningDetailsContent(
    screening: Screening,
    reservationState: ReservationUiState,
    onReserveClick: () -> Unit,
    reservationStatus: ReservationStatusUiState,
    purchaseState: PurchaseUiState,
    onPurchaseClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = screening.movieTitle, style = MaterialTheme.typography.headlineSmall)
        Text(text = "Kategorija: ${screening.category}", style = MaterialTheme.typography.bodyMedium)
        Text(text = formatScreeningTime(screening.screeningTime), style = MaterialTheme.typography.bodyMedium)
        if (screening.description.isNotBlank()) {
            Text(text = screening.description, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = if (screening.availableSeats > 0) {
                "Slobodno: ${screening.availableSeats} / ${screening.totalSeats} mjesta"
            } else {
                "Rasprodano"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        if (screening.availableSeats > 0) {
            Button(
                onClick = onReserveClick,
                enabled = reservationState !is ReservationUiState.InProgress,
            ) {
                Text(if (reservationState is ReservationUiState.InProgress) "Rezerviranje..." else "Rezerviraj")
            }
            Button(
                onClick = onPurchaseClick,
                enabled = purchaseState !is PurchaseUiState.InProgress,
            ) {
                Text(if (purchaseState is PurchaseUiState.InProgress) "Kupnja u tijeku..." else "Kupi")
            }
        }
        when (reservationState) {
            is ReservationUiState.Error -> Text(
                text = reservationState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )

            else -> {}
        }
        when (reservationStatus) {
            is ReservationStatusUiState.Active -> {
                val (label, color) = when (reservationStatus.status) {
                    ReservationStatus.PENDING -> "Status rezervacije: na čekanju" to MaterialTheme.colorScheme.tertiary
                    ReservationStatus.APPROVED -> "Status rezervacije: odobreno" to MaterialTheme.colorScheme.primary
                    ReservationStatus.REJECTED -> "Status rezervacije: odbijeno" to MaterialTheme.colorScheme.error
                }
                Text(text = label, color = color, style = MaterialTheme.typography.bodyMedium)
            }

            is ReservationStatusUiState.Error -> Text(
                text = reservationStatus.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )

            else -> {}
        }
        when (purchaseState) {
            is PurchaseUiState.Success -> Text(
                text = "Kupnja uspješna! Karta je zabilježena.",
                style = MaterialTheme.typography.bodyMedium,
            )

            is PurchaseUiState.Error -> Text(
                text = purchaseState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )

            else -> {}
        }
    }
}
