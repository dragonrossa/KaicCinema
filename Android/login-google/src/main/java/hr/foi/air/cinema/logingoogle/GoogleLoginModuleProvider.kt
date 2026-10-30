package hr.foi.air.cinema.logingoogle

import android.content.Context
import hr.foi.air.cinema.core.AnalyticsLogger
import hr.foi.air.cinema.core.LoginModule
import hr.foi.air.cinema.core.LoginModuleProvider
import hr.foi.air.cinema.core.UserRoleResolver

private const val WEB_CLIENT_ID_RESOURCE_NAME = "default_web_client_id"

class GoogleLoginModuleProvider : LoginModuleProvider {

    override val id: String = "google"
    override val displayName: String = "Google"

    override fun create(
        context: Context,
        analyticsLogger: AnalyticsLogger,
        userRoleResolver: UserRoleResolver,
    ): LoginModule {
        val resId = context.resources.getIdentifier(
            WEB_CLIENT_ID_RESOURCE_NAME,
            "string",
            context.packageName,
        )
        check(resId != 0) { "Resource $WEB_CLIENT_ID_RESOURCE_NAME not found - is google-services.json configured?" }

        return GoogleLoginModule(
            analyticsLogger = analyticsLogger,
            userRoleResolver = userRoleResolver,
            webClientId = context.getString(resId),
        )
    }
}
