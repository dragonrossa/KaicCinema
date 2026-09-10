package hr.foi.air.cinema.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import hr.foi.air.cinema.data.UserRole
import hr.foi.air.cinema.ui.admin.AddNewsPage
import hr.foi.air.cinema.ui.admin.AddScreeningPage
import hr.foi.air.cinema.ui.admin.AdminPanelPage
import hr.foi.air.cinema.ui.admin.ManageScreeningsPage
import hr.foi.air.cinema.ui.admin.ReservationRequestsPage
import hr.foi.air.cinema.ui.auth.LoginPage
import hr.foi.air.cinema.ui.news.NewsPage
import hr.foi.air.cinema.ui.screenings.ScreeningDetailsPage
import hr.foi.air.cinema.ui.screenings.ScreeningsPage

private const val ARG_SCREENING_ID = "screeningId"
private const val ROUTE_LOGIN = "login"
private const val ROUTE_SCREENINGS = "screenings"
private const val ROUTE_SCREENING_DETAILS = "screeningDetails/{$ARG_SCREENING_ID}"
private const val ROUTE_ADMIN_PANEL = "adminPanel"
private const val ROUTE_MANAGE_SCREENINGS = "manageScreenings"
private const val ROUTE_ADD_SCREENING = "addScreening"
private const val ROUTE_ADD_NEWS = "addNews"
private const val ROUTE_NEWS = "news"
private const val ROUTE_RESERVATION_REQUESTS = "reservationRequests"

@Composable
fun CinemaNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val goToLogin: () -> Unit = {
        navController.navigate(ROUTE_LOGIN) {
            popUpTo(0)
        }
    }

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
                onNewsClick = { navController.navigate(ROUTE_NEWS) },
                onLogout = goToLogin,
            )
        }
        composable(ROUTE_NEWS) {
            NewsPage(onBackClick = { navController.popBackStack() })
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
                onUnauthorized = goToLogin,
                onLogout = goToLogin,
                onManageScreeningsClick = { navController.navigate(ROUTE_MANAGE_SCREENINGS) },
                onPublishNewsClick = { navController.navigate(ROUTE_ADD_NEWS) },
                onViewReservationRequestsClick = { navController.navigate(ROUTE_RESERVATION_REQUESTS) },
            )
        }
        composable(ROUTE_RESERVATION_REQUESTS) {
            ReservationRequestsPage(onBackClick = { navController.popBackStack() })
        }
        composable(ROUTE_MANAGE_SCREENINGS) {
            ManageScreeningsPage(
                onAddScreeningClick = { navController.navigate(ROUTE_ADD_SCREENING) },
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(ROUTE_ADD_SCREENING) {
            AddScreeningPage(
                onScreeningAdded = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(ROUTE_ADD_NEWS) {
            AddNewsPage(
                onNewsPublished = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
