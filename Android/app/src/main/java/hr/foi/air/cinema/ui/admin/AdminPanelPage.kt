package hr.foi.air.cinema.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.foi.air.cinema.data.Purchase
import hr.foi.air.cinema.data.Reservation
import hr.foi.air.cinema.ui.auth.LogoutButton
import hr.foi.air.cinema.ui.common.formatScreeningTime

private const val FEATURE_MANAGE_SCREENINGS = "Upravljanje projekcijama"
private const val FEATURE_PUBLISH_NEWS = "Objava novosti"

private val ADMIN_FEATURES = listOf(
    FEATURE_MANAGE_SCREENINGS,
    FEATURE_PUBLISH_NEWS,
    "Odobravanje zahtjeva za rezervaciju",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelPage(
    onUnauthorized: () -> Unit,
    onLogout: () -> Unit,
    onManageScreeningsClick: () -> Unit,
    onPublishNewsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminAccessViewModel = viewModel(),
    bookingsViewModel: AdminBookingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val bookingsState by bookingsViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AdminAccessUiState.Denied) {
            onUnauthorized()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Admin panel") },
                actions = { LogoutButton(onLoggedOut = onLogout) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (uiState) {
                is AdminAccessUiState.Checking -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is AdminAccessUiState.Denied -> {
                    Text(
                        text = "Nemate ovlasti za pristup ovom ekranu",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                is AdminAccessUiState.Authorized -> {
                    AdminPanelContent(
                        bookingsState = bookingsState,
                        onManageScreeningsClick = onManageScreeningsClick,
                        onPublishNewsClick = onPublishNewsClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminPanelContent(
    bookingsState: AdminBookingsUiState,
    onManageScreeningsClick: () -> Unit,
    onPublishNewsClick: () -> Unit,
) {
    val featureClickHandlers = mapOf(
        FEATURE_MANAGE_SCREENINGS to onManageScreeningsClick,
        FEATURE_PUBLISH_NEWS to onPublishNewsClick,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ADMIN_FEATURES.forEach { feature ->
            val onFeatureClick = featureClickHandlers[feature]
            val clickModifier = if (onFeatureClick != null) {
                Modifier.clickable(onClick = onFeatureClick)
            } else {
                Modifier
            }
            Card(modifier = Modifier.fillMaxWidth().then(clickModifier)) {
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        Text(text = "Rezervacije i kupnje", style = MaterialTheme.typography.titleMedium)

        when (bookingsState) {
            is AdminBookingsUiState.Loading -> {
                Text(text = "Učitavanje...", style = MaterialTheme.typography.bodyMedium)
            }

            is AdminBookingsUiState.Error -> {
                Text(
                    text = bookingsState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            is AdminBookingsUiState.Success -> {
                if (bookingsState.reservations.isEmpty() && bookingsState.purchases.isEmpty()) {
                    Text(text = "Nema zabilježenih rezervacija ni kupnji", style = MaterialTheme.typography.bodyMedium)
                }
                bookingsState.reservations.forEach { reservation -> ReservationRow(reservation) }
                bookingsState.purchases.forEach { purchase -> PurchaseRow(purchase) }
            }
        }
    }
}

@Composable
private fun ReservationRow(reservation: Reservation) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Rezervacija", style = MaterialTheme.typography.labelMedium)
            Text(text = "Korisnik: ${reservation.userId}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Projekcija: ${reservation.screeningId}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${reservation.status}", style = MaterialTheme.typography.bodyMedium)
            Text(text = formatScreeningTime(reservation.createdAt), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PurchaseRow(purchase: Purchase) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Kupnja", style = MaterialTheme.typography.labelMedium)
            Text(text = "Korisnik: ${purchase.userId}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Projekcija: ${purchase.screeningId}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${purchase.status}", style = MaterialTheme.typography.bodyMedium)
            Text(text = formatScreeningTime(purchase.createdAt), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
