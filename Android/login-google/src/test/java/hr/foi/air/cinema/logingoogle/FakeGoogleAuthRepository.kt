package hr.foi.air.cinema.logingoogle

class FakeGoogleAuthRepository(
    private val signInResult: Result<Unit> = Result.success(Unit),
    private val userId: String? = "test-uid",
) : GoogleAuthRepository {

    var signInCallCount = 0
        private set

    override suspend fun signInWithIdToken(idToken: String): Result<Unit> {
        signInCallCount++
        return signInResult
    }

    override fun currentUserId(): String? = userId
}
