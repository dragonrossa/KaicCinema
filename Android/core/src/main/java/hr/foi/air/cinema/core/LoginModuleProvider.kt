package hr.foi.air.cinema.core

import android.content.Context

interface LoginModuleProvider {
    val id: String
    val displayName: String

    fun create(
        context: Context,
        analyticsLogger: AnalyticsLogger,
        userRoleResolver: UserRoleResolver,
    ): LoginModule
}
