package hr.foi.air.cinema.loginemail

import android.content.Context
import hr.foi.air.cinema.core.AnalyticsLogger
import hr.foi.air.cinema.core.LoginModule
import hr.foi.air.cinema.core.LoginModuleProvider
import hr.foi.air.cinema.core.UserRoleResolver

class EmailLoginModuleProvider : LoginModuleProvider {

    override val id: String = "email"
    override val displayName: String = "Email"

    override fun create(
        context: Context,
        analyticsLogger: AnalyticsLogger,
        userRoleResolver: UserRoleResolver,
    ): LoginModule = EmailLoginModule(
        analyticsLogger = analyticsLogger,
        userRoleResolver = userRoleResolver,
    )
}
