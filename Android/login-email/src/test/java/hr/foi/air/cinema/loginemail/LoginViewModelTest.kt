package hr.foi.air.cinema.loginemail

import hr.foi.air.cinema.core.AnalyticsEvents
import hr.foi.air.cinema.core.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_blankCredentials_showsErrorWithoutCallingRepository() {
        val authRepository = FakeAuthRepository()
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = authRepository,
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "", password = "")

        assertTrue(viewModel.uiState.value is LoginUiState.Error)
        assertEquals(0, authRepository.loginCallCount)
    }

    @Test
    fun login_validCredentials_userRole_updatesStateToSuccessWithUserRole() = runTest {
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.success(UserRole.USER)),
            authRepository = FakeAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success(UserRole.USER), viewModel.uiState.value)
    }

    @Test
    fun login_validCredentials_adminRole_updatesStateToSuccessWithAdminRole() = runTest {
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.success(UserRole.ADMIN)),
            authRepository = FakeAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "admin@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success(UserRole.ADMIN), viewModel.uiState.value)
    }

    @Test
    fun login_authFailure_updatesStateToGenericErrorMessage() = runTest {
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeAuthRepository(loginResult = Result.failure(Exception("some unmapped error"))),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "user@example.com", password = "wrong")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Prijava nije uspjela", (state as LoginUiState.Error).message)
    }

    @Test
    fun login_invalidCredentials_updatesStateToLocalizedErrorMessage() = runTest {
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeAuthRepository(
                loginResult = Result.failure(
                    com.google.firebase.auth.FirebaseAuthInvalidCredentialsException(
                        "ERROR_INVALID_CREDENTIAL",
                        "The supplied auth credential is incorrect, malformed or has expired.",
                    ),
                ),
            ),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "user@example.com", password = "wrong")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Pogrešan email ili lozinka", (state as LoginUiState.Error).message)
    }

    @Test
    fun login_roleFetchFailure_updatesStateToError() = runTest {
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.failure(Exception("Greška pri dohvaćanju uloge"))),
            authRepository = FakeAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginUiState.Error)
    }

    @Test
    fun login_success_registersFcmTokenForCurrentUser() = runTest {
        val fcmTokenProvider = FakeFcmTokenProvider(tokenResult = Result.success("device-token-1"))
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.success(UserRole.USER)),
            authRepository = FakeAuthRepository(userId = "test-uid"),
            fcmTokenProvider = fcmTokenProvider,
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fcmTokenProvider.getTokenCallCount)
        assertEquals(1, fcmTokenProvider.saveTokenCallCount)
        assertEquals("test-uid", fcmTokenProvider.lastSavedUid)
        assertEquals("device-token-1", fcmTokenProvider.lastSavedToken)
    }

    @Test
    fun login_success_subscribesToNewScreeningsTopic() = runTest {
        val fcmTokenProvider = FakeFcmTokenProvider()
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.success(UserRole.USER)),
            authRepository = FakeAuthRepository(),
            fcmTokenProvider = fcmTokenProvider,
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fcmTokenProvider.subscribeToNewScreeningsCallCount)
    }

    @Test
    fun login_success_subscribesToScreeningNotificationsTopic() = runTest {
        val fcmTokenProvider = FakeFcmTokenProvider()
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.success(UserRole.USER)),
            authRepository = FakeAuthRepository(),
            fcmTokenProvider = fcmTokenProvider,
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fcmTokenProvider.subscribeToScreeningNotificationsCallCount)
    }

    @Test
    fun login_success_fcmTokenFetchFails_stillUpdatesStateToSuccess() = runTest {
        val fcmTokenProvider = FakeFcmTokenProvider(tokenResult = Result.failure(Exception("Nema tokena")))
        val viewModel = LoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.success(UserRole.USER)),
            authRepository = FakeAuthRepository(),
            fcmTokenProvider = fcmTokenProvider,
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success(UserRole.USER), viewModel.uiState.value)
        assertEquals(0, fcmTokenProvider.saveTokenCallCount)
    }

    @Test
    fun login_success_logsLoginAnalyticsEvent() = runTest {
        val analyticsLogger = FakeAnalyticsLogger()
        val viewModel = LoginViewModel(
            analyticsLogger = analyticsLogger,
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.success(UserRole.USER)),
            authRepository = FakeAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, analyticsLogger.loggedEvents.size)
        assertEquals(AnalyticsEvents.LOGIN, analyticsLogger.loggedEvents.first().first)
    }

    @Test
    fun login_authFailure_doesNotLogLoginAnalyticsEvent() = runTest {
        val analyticsLogger = FakeAnalyticsLogger()
        val viewModel = LoginViewModel(
            analyticsLogger = analyticsLogger,
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeAuthRepository(loginResult = Result.failure(Exception("Neispravni podaci"))),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "user@example.com", password = "wrong")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(analyticsLogger.loggedEvents.isEmpty())
    }
}
