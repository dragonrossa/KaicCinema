package hr.foi.air.cinema.ui.admin

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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminAccessViewModelTest {

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
    fun access_adminRole_authorized() = runTest {
        val viewModel = AdminAccessViewModel(
            authRepository = FakeAuthRepository(userId = "uid"),
            userRepository = FakeUserRepository(roleResult = Result.success(UserRole.ADMIN)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AdminAccessUiState.Authorized, viewModel.uiState.value)
    }

    @Test
    fun access_userRole_denied() = runTest {
        val viewModel = AdminAccessViewModel(
            authRepository = FakeAuthRepository(userId = "uid"),
            userRepository = FakeUserRepository(roleResult = Result.success(UserRole.USER)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AdminAccessUiState.Denied, viewModel.uiState.value)
    }

    @Test
    fun access_noCurrentUser_denied() = runTest {
        val viewModel = AdminAccessViewModel(
            authRepository = FakeAuthRepository(userId = null),
            userRepository = FakeUserRepository(roleResult = Result.success(UserRole.ADMIN)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AdminAccessUiState.Denied, viewModel.uiState.value)
    }

    @Test
    fun access_roleFetchFailure_denied() = runTest {
        val viewModel = AdminAccessViewModel(
            authRepository = FakeAuthRepository(userId = "uid"),
            userRepository = FakeUserRepository(roleResult = Result.failure(Exception("error"))),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AdminAccessUiState.Denied, viewModel.uiState.value)
    }
}
