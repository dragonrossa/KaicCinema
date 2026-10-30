package hr.foi.air.cinema.ui.navigation

import hr.foi.air.cinema.core.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SCRUM-137: [loginDestinationFor] is the single place that decides where the app navigates
 * after a successful login. Both EmailLoginModule and GoogleLoginModule funnel their success
 * callback through this same function, so testing it here is a direct regression guard that
 * either login path resolves to the same destination for a given role.
 */
class CinemaNavHostRegressionTest {

    @Test
    fun loginDestinationFor_adminRole_resolvesToAdminPanel() {
        assertEquals(ROUTE_ADMIN_PANEL, loginDestinationFor(UserRole.ADMIN))
    }

    @Test
    fun loginDestinationFor_userRole_resolvesToScreenings() {
        assertEquals(ROUTE_SCREENINGS, loginDestinationFor(UserRole.USER))
    }
}
