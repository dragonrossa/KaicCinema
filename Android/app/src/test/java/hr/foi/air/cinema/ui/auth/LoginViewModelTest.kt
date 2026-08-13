package hr.foi.air.cinema.ui.auth

import hr.foi.air.cinema.data.FakeAuthRepository
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
        val viewModel = LoginViewModel(authRepository, FakeUserRepository())

        viewModel.login(email = "", password = "")

        assertTrue(viewModel.uiState.value is LoginUiState.Error)
        assertEquals(0, authRepository.loginCallCount)
    }

    @Test
    fun login_validCredentials_userRole_updatesStateToSuccessWithUserRole() = runTest {
        val viewModel = LoginViewModel(
            authRepository = FakeAuthRepository(),
            userRepository = FakeUserRepository(roleResult = Result.success(UserRole.USER)),
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
        )

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginUiState.Error)
    }
}
