package hr.foi.air.cinema.logingoogle

import androidx.compose.runtime.Composable
import hr.foi.air.cinema.core.AnalyticsLogger
import hr.foi.air.cinema.core.LoginModule
import hr.foi.air.cinema.core.UserRole
import hr.foi.air.cinema.core.UserRoleResolver

class GoogleLoginModule(
    private val analyticsLogger: AnalyticsLogger,
    private val userRoleResolver: UserRoleResolver,
    private val webClientId: String,
) : LoginModule {

    override val id: String = "google"
    override val displayName: String = "Google"

    @Composable
    override fun Content(onLoginSuccess: (UserRole) -> Unit) {
        GoogleLoginPage(
            analyticsLogger = analyticsLogger,
            userRoleResolver = userRoleResolver,
            webClientId = webClientId,
            onLoginSuccess = onLoginSuccess,
        )
    }
}
