package hr.foi.air.cinema.loginemail

class FakeFcmTokenProvider(
    private val tokenResult: Result<String> = Result.success("fake-fcm-token"),
    private val saveTokenResult: Result<Unit> = Result.success(Unit),
    private val subscribeResult: Result<Unit> = Result.success(Unit),
) : FcmTokenProvider {

    var getTokenCallCount = 0
        private set
    var saveTokenCallCount = 0
        private set
    var lastSavedUid: String? = null
        private set
    var lastSavedToken: String? = null
        private set
    var subscribeToNewScreeningsCallCount = 0
        private set
    var subscribeToScreeningNotificationsCallCount = 0
        private set

    override suspend fun getToken(): Result<String> {
        getTokenCallCount++
        return tokenResult
    }

    override suspend fun saveToken(uid: String, token: String): Result<Unit> {
        saveTokenCallCount++
        lastSavedUid = uid
        lastSavedToken = token
        return saveTokenResult
    }

    override suspend fun subscribeToNewScreenings(): Result<Unit> {
        subscribeToNewScreeningsCallCount++
        return subscribeResult
    }

    override suspend fun subscribeToScreeningNotifications(): Result<Unit> {
        subscribeToScreeningNotificationsCallCount++
        return subscribeResult
    }
}
