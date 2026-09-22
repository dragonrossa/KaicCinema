package hr.foi.air.cinema.ui.auth

import hr.foi.air.cinema.data.FakeAuthRepository
import hr.foi.air.cinema.data.FakeFcmTokenProvider
import hr.foi.air.cinema.data.FakeUserRepository
import hr.foi.air.cinema.data.UserRole
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
        val viewModel = LoginViewModel(authRepository, FakeUserRepository(), FakeFcmTokenProvider())

        viewModel.login(email = "", password = "")

        assertTrue(viewModel.uiState.value is LoginUiState.Error)
        assertEquals(0, authRepository.loginCallCount)
    }

    @Test
    fun login_validCredentials_userRole_updatesStateToSuccessWithUserRole() = runTest {
        val viewModel = LoginViewModel(
            authRepository = FakeAuthRepository(),
            userRepository = FakeUserRepository(roleResult = Result.success(UserRole.USER)),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success(UserRole.USER), viewModel.uiState.value)
    }

    @Test
    fun login_validCredentials_adminRole_updatesStateToSuccessWithAdminRole() = runTest {
        val viewModel = LoginViewModel(
            authRepository = FakeAuthRepository(),
            userRepository = FakeUserRepository(roleResult = Result.success(UserRole.ADMIN)),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "admin@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success(UserRole.ADMIN), viewModel.uiState.value)
    }

    @Test
    fun login_authFailure_updatesStateToError() = runTest {
        val viewModel = LoginViewModel(
            authRepository = FakeAuthRepository(loginResult = Result.failure(Exception("Neispravni podaci"))),
            userRepository = FakeUserRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "user@example.com", password = "wrong")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Neispravni podaci", (state as LoginUiState.Error).message)
    }

    @Test
    fun login_roleFetchFailure_updatesStateToError() = runTest {
        val viewModel = LoginViewModel(
            authRepository = FakeAuthRepository(),
            userRepository = FakeUserRepository(roleResult = Result.failure(Exception("Greška pri dohvaćanju uloge"))),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginUiState.Error)
    }

    @Test
    fun login_success_registersFcmTokenForCurrentUser() = runTest {
        val userRepository = FakeUserRepository(roleResult = Result.success(UserRole.USER))
        val fcmTokenProvider = FakeFcmTokenProvider(tokenResult = Result.success("device-token-1"))
        val viewModel = LoginViewModel(
            authRepository = FakeAuthRepository(userId = "test-uid"),
            userRepository = userRepository,
            fcmTokenProvider = fcmTokenProvider,
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fcmTokenProvider.getTokenCallCount)
        assertEquals(1, userRepository.updateFcmTokenCallCount)
        assertEquals("test-uid", userRepository.lastFcmTokenUid)
        assertEquals("device-token-1", userRepository.lastFcmToken)
    }

    @Test
    fun login_success_subscribesToNewScreeningsTopic() = runTest {
        val fcmTokenProvider = FakeFcmTokenProvider()
        val viewModel = LoginViewModel(
            authRepository = FakeAuthRepository(),
            userRepository = FakeUserRepository(roleResult = Result.success(UserRole.USER)),
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
            authRepository = FakeAuthRepository(),
            userRepository = FakeUserRepository(roleResult = Result.success(UserRole.USER)),
            fcmTokenProvider = fcmTokenProvider,
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fcmTokenProvider.subscribeToScreeningNotificationsCallCount)
    }

    @Test
    fun login_success_fcmTokenFetchFails_stillUpdatesStateToSuccess() = runTest {
        val userRepository = FakeUserRepository(roleResult = Result.success(UserRole.USER))
        val viewModel = LoginViewModel(
            authRepository = FakeAuthRepository(),
            userRepository = userRepository,
            fcmTokenProvider = FakeFcmTokenProvider(tokenResult = Result.failure(Exception("Nema tokena"))),
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success(UserRole.USER), viewModel.uiState.value)
        assertEquals(0, userRepository.updateFcmTokenCallCount)
    }
}
