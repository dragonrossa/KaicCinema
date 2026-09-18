package hr.foi.air.cinema.data

class FakeFcmTokenProvider(
    private val tokenResult: Result<String> = Result.success("fake-fcm-token"),
) : FcmTokenProvider {

    var getTokenCallCount = 0
        private set

    override suspend fun getToken(): Result<String> {
        getTokenCallCount++
        return tokenResult
    }
}
