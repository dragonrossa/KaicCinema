package hr.foi.air.cinema.logingoogle

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
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
class GoogleLoginViewModelTest {

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
    fun initialState_isIdle() {
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeGoogleAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun onSignInStarted_updatesStateToLoading() {
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeGoogleAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.onSignInStarted()

        assertEquals(LoginUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun onCredentialFetched_success_userRole_updatesStateToSuccessWithUserRole() = runTest {
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.success(UserRole.USER)),
            authRepository = FakeGoogleAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.onCredentialFetched(Result.success("fake-id-token"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success(UserRole.USER), viewModel.uiState.value)
    }

    @Test
    fun onCredentialFetched_success_adminRole_updatesStateToSuccessWithAdminRole() = runTest {
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.success(UserRole.ADMIN)),
            authRepository = FakeGoogleAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.onCredentialFetched(Result.success("fake-id-token"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success(UserRole.ADMIN), viewModel.uiState.value)
    }

    @Test
    fun onCredentialFetched_credentialFetchCancelled_updatesStateToIdle() = runTest {
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeGoogleAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.onSignInStarted()
        viewModel.onCredentialFetched(Result.failure(GetCredentialCancellationException()))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun onCredentialFetched_noCredentialAvailable_updatesStateToLocalizedErrorMessage() = runTest {
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeGoogleAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.onCredentialFetched(Result.failure(NoCredentialException()))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Nema dostupnog Google računa na uređaju", (state as LoginUiState.Error).message)
    }

    @Test
    fun onCredentialFetched_unmappedError_updatesStateToGenericErrorMessage() = runTest {
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeGoogleAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.onCredentialFetched(Result.failure(RuntimeException("unexpected")))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Prijava putem Googlea nije uspjela", (state as LoginUiState.Error).message)
    }

    @Test
    fun onCredentialFetched_firebaseSignInFailure_updatesStateToGenericErrorMessage() = runTest {
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeGoogleAuthRepository(signInResult = Result.failure(RuntimeException("sign-in failed"))),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.onCredentialFetched(Result.success("fake-id-token"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Prijava putem Googlea nije uspjela", (state as LoginUiState.Error).message)
    }

    @Test
    fun onCredentialFetched_roleFetchFailure_updatesStateToError() = runTest {
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(roleResult = Result.failure(RuntimeException("Greška pri dohvaćanju uloge"))),
            authRepository = FakeGoogleAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.onCredentialFetched(Result.success("fake-id-token"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Greška pri dohvaćanju uloge", (state as LoginUiState.Error).message)
    }

    @Test
    fun onCredentialFetched_success_logsLoginAnalyticsEvent() = runTest {
        val analyticsLogger = FakeAnalyticsLogger()
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = analyticsLogger,
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeGoogleAuthRepository(),
            fcmTokenProvider = FakeFcmTokenProvider(),
        )

        viewModel.onCredentialFetched(Result.success("fake-id-token"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, analyticsLogger.loggedEvents.size)
        assertEquals(AnalyticsEvents.LOGIN, analyticsLogger.loggedEvents.first().first)
    }

    @Test
    fun onCredentialFetched_success_savesFcmToken() = runTest {
        val fcmTokenProvider = FakeFcmTokenProvider()
        val viewModel = GoogleLoginViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            userRoleResolver = FakeUserRoleResolver(),
            authRepository = FakeGoogleAuthRepository(userId = "google-uid"),
            fcmTokenProvider = fcmTokenProvider,
        )

        viewModel.onCredentialFetched(Result.success("fake-id-token"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fcmTokenProvider.saveTokenCallCount)
        assertEquals("google-uid", fcmTokenProvider.lastSavedUid)
    }
}
