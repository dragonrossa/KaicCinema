package hr.foi.air.cinema.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import hr.foi.air.cinema.data.UserRole
import hr.foi.air.cinema.ui.admin.AdminPanelPage
import hr.foi.air.cinema.ui.auth.LoginPage
import hr.foi.air.cinema.ui.screenings.ScreeningDetailsPage
import hr.foi.air.cinema.ui.screenings.ScreeningsPage

private const val ARG_SCREENING_ID = "screeningId"
private const val ROUTE_LOGIN = "login"
private const val ROUTE_SCREENINGS = "screenings"
private const val ROUTE_SCREENING_DETAILS = "screeningDetails/{$ARG_SCREENING_ID}"
private const val ROUTE_ADMIN_PANEL = "adminPanel"

@Composable
fun CinemaNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_LOGIN,
        modifier = modifier,
    ) {
        composable(ROUTE_LOGIN) {
            LoginPage(
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        UserRole.ADMIN -> ROUTE_ADMIN_PANEL
                        UserRole.USER -> ROUTE_SCREENINGS
                    }
                    navController.navigate(destination) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(ROUTE_SCREENINGS) {
            ScreeningsPage(
                onScreeningSelected = { screeningId ->
                    navController.navigate("screeningDetails/$screeningId")
                },
            )
        }
        composable(
            route = ROUTE_SCREENING_DETAILS,
            arguments = listOf(navArgument(ARG_SCREENING_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val screeningId = backStackEntry.arguments?.getString(ARG_SCREENING_ID).orEmpty()
            ScreeningDetailsPage(
                screeningId = screeningId,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(ROUTE_ADMIN_PANEL) {
            AdminPanelPage(
                onUnauthorized = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(0)
                    }
                },
            )
        }
    }
}
