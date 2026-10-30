package hr.foi.air.cinema.logingoogle

import androidx.credentials.exceptions.NoCredentialException

internal fun mapGoogleAuthErrorMessage(error: Throwable): String = when (error) {
    is NoCredentialException -> "Nema dostupnog Google računa na uređaju"
    else -> "Prijava putem Googlea nije uspjela"
}
