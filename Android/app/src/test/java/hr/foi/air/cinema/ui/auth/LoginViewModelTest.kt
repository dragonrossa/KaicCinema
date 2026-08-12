package hr.foi.air.cinema.ui.auth

import hr.foi.air.cinema.data.AuthRepository
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
        val repository = FakeAuthRepository(result = Result.success(Unit))
        val viewModel = LoginViewModel(repository)

        viewModel.login(email = "", password = "")

        assertTrue(viewModel.uiState.value is LoginUiState.Error)
        assertEquals(0, repository.loginCallCount)
    }

    @Test
    fun login_validCredentials_success_updatesStateToSuccess() = runTest {
        val repository = FakeAuthRepository(result = Result.success(Unit))
        val viewModel = LoginViewModel(repository)

        viewModel.login(email = "user@example.com", password = "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun login_validCredentials_failure_updatesStateToError() = runTest {
        val repository = FakeAuthRepository(result = Result.failure(Exception("Neispravni podaci")))
        val viewModel = LoginViewModel(repository)

        viewModel.login(email = "user@example.com", password = "wrong")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Neispravni podaci", (state as LoginUiState.Error).message)
    }

    private class FakeAuthRepository(private val result: Result<Unit>) : AuthRepository {
        var loginCallCount = 0
            private set

        override suspend fun login(email: String, password: String): Result<Unit> {
            loginCallCount++
            return result
        }
    }
}
