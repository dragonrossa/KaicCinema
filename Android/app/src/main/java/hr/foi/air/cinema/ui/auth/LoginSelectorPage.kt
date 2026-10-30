package hr.foi.air.cinema.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import hr.foi.air.cinema.core.AnalyticsLogger
import hr.foi.air.cinema.core.LoginModule
import hr.foi.air.cinema.core.LoginModuleProvider
import hr.foi.air.cinema.core.UserRole
import hr.foi.air.cinema.core.UserRoleResolver

@Composable
fun LoginSelectorPage(
    providers: List<LoginModuleProvider>,
    analyticsLogger: AnalyticsLogger,
    userRoleResolver: UserRoleResolver,
    onLoginSuccess: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedModule by remember { mutableStateOf<LoginModule?>(null) }

    val module = selectedModule
    if (module != null) {
        BackHandler { selectedModule = null }
        Column(modifier = modifier.fillMaxSize()) {
            TextButton(onClick = { selectedModule = null }) {
                Text("← Natrag")
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                module.Content(onLoginSuccess = onLoginSuccess)
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Cinema", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Odaberite način prijave",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        providers.forEach { provider ->
            Button(
                onClick = {
                    selectedModule = provider.create(
                        context = context,
                        analyticsLogger = analyticsLogger,
                        userRoleResolver = userRoleResolver,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(provider.displayName)
            }
        }
    }
}
