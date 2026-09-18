package hr.foi.air.cinema.data

class FakeUserRepository(
    private val roleResult: Result<UserRole> = Result.success(UserRole.USER),
    private val updateFcmTokenResult: Result<Unit> = Result.success(Unit),
) : UserRepository {

    var updateFcmTokenCallCount = 0
        private set
    var lastFcmTokenUid: String? = null
        private set
    var lastFcmToken: String? = null
        private set

    override suspend fun getUserRole(uid: String): Result<UserRole> = roleResult

    override suspend fun updateFcmToken(uid: String, token: String): Result<Unit> {
        updateFcmTokenCallCount++
        lastFcmTokenUid = uid
        lastFcmToken = token
        return updateFcmTokenResult
    }
}
