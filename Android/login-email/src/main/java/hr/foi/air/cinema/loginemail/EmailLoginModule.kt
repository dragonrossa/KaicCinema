package hr.foi.air.cinema.loginemail

import androidx.compose.runtime.Composable
import hr.foi.air.cinema.core.AnalyticsLogger
import hr.foi.air.cinema.core.LoginModule
import hr.foi.air.cinema.core.UserRole
import hr.foi.air.cinema.core.UserRoleResolver

class EmailLoginModule(
    private val analyticsLogger: AnalyticsLogger,
    private val userRoleResolver: UserRoleResolver,
) : LoginModule {

    override val id: String = "email"
    override val displayName: String = "Email"

    @Composable
    override fun Content(onLoginSuccess: (UserRole) -> Unit) {
        LoginPage(
            analyticsLogger = analyticsLogger,
            userRoleResolver = userRoleResolver,
            onLoginSuccess = onLoginSuccess,
        )
    }
}
