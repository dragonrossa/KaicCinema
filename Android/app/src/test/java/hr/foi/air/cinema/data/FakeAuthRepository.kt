package hr.foi.air.cinema.data

import hr.foi.air.cinema.loginemail.AuthRepository

class FakeAuthRepository(
    private val loginResult: Result<Unit> = Result.success(Unit),
    private val userId: String? = "test-uid",
) : AuthRepository {

    var loginCallCount = 0
        private set
    var logoutCallCount = 0
        private set

    override suspend fun login(email: String, password: String): Result<Unit> {
        loginCallCount++
        return loginResult
    }

    override fun currentUserId(): String? = userId

    override fun logout() {
        logoutCallCount++
    }
}
