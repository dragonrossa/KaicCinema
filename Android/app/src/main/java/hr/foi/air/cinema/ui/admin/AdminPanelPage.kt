package hr.foi.air.cinema.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import hr.foi.air.cinema.ui.auth.LogoutButton

private val ADMIN_FEATURES = listOf(
    "Upravljanje projekcijama",
    "Objava novosti",
    "Odobravanje zahtjeva za rezervaciju",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelPage(
    onUnauthorized: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminAccessViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    AdminPanelContent()
                }
            }
        }
    }
}

@Composable
private fun AdminPanelContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ADMIN_FEATURES.forEach { feature ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
