package hr.foi.air.cinema.core

import androidx.compose.runtime.Composable

interface LoginModule {
    val id: String
    val displayName: String

    @Composable
    fun Content(onLoginSuccess: (UserRole) -> Unit)
}
