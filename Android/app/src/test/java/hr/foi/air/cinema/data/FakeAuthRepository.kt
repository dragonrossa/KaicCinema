package hr.foi.air.cinema.data

class FakeAuthRepository(
    private val loginResult: Result<Unit> = Result.success(Unit),
    private val userId: String? = "test-uid",
) : AuthRepository {

    var loginCallCount = 0
        private set

    override suspend fun login(email: String, password: String): Result<Unit> {
        loginCallCount++
        return loginResult
    }

    override fun currentUserId(): String? = userId
}
