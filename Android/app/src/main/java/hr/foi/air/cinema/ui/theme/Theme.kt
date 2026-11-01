package hr.foi.air.cinema.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CinemaColorScheme = lightColorScheme(
    primary = CinemaBurgundy,
    onPrimary = CinemaWhite,
    secondary = CinemaGold,
    onSecondary = CinemaOnDark,
    background = CinemaCream,
    onBackground = CinemaOnDark,
    surface = CinemaWhite,
    onSurface = CinemaOnDark,
    error = CinemaError,
    onError = CinemaWhite,
)

@Composable
fun CinemaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinemaColorScheme,
        typography = Typography,
        content = content
    )
}
