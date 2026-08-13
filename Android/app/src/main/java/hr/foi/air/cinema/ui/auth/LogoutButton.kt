package hr.foi.air.cinema.ui.auth

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LogoutButton(
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogoutViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LogoutUiState.LoggedOut) {
            onLoggedOut()
        }
    }

    TextButton(onClick = { viewModel.logout() }, modifier = modifier) {
        Text("Odjava")
    }
}
