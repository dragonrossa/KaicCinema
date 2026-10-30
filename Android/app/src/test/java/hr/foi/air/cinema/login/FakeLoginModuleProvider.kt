package hr.foi.air.cinema.login

import android.content.Context
import hr.foi.air.cinema.core.AnalyticsLogger
import hr.foi.air.cinema.core.LoginModule
import hr.foi.air.cinema.core.LoginModuleProvider
import hr.foi.air.cinema.core.UserRole
import hr.foi.air.cinema.core.UserRoleResolver

class FakeLoginModuleProvider : LoginModuleProvider {

    override val id: String = "fake-test-provider"
    override val displayName: String = "Fake Test Provider"

    override fun create(
        context: Context,
        analyticsLogger: AnalyticsLogger,
        userRoleResolver: UserRoleResolver,
    ): LoginModule = object : LoginModule {
        override val id: String = this@FakeLoginModuleProvider.id
        override val displayName: String = this@FakeLoginModuleProvider.displayName

        @androidx.compose.runtime.Composable
        override fun Content(onLoginSuccess: (UserRole) -> Unit) = Unit
    }
}
