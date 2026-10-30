package hr.foi.air.cinema.logingoogle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hr.foi.air.cinema.core.AnalyticsLogger
import hr.foi.air.cinema.core.UserRole
import hr.foi.air.cinema.core.UserRoleResolver
import kotlinx.coroutines.launch

@Composable
internal fun GoogleLoginPage(
    analyticsLogger: AnalyticsLogger,
    userRoleResolver: UserRoleResolver,
    webClientId: String,
    onLoginSuccess: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoogleLoginViewModel = viewModel(
        factory = viewModelFactory {
            initializer { GoogleLoginViewModel(analyticsLogger = analyticsLogger, userRoleResolver = userRoleResolver) }
        },
    ),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is LoginUiState.Success) {
            onLoginSuccess(state.role)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        val state = uiState
        if (state is LoginUiState.Error) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Button(
            onClick = {
                viewModel.onSignInStarted()
                coroutineScope.launch {
                    viewModel.onCredentialFetched(fetchGoogleIdToken(context, webClientId))
                }
            },
            enabled = state !is LoginUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state is LoginUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Sign in with Google")
            }
        }
    }
}
