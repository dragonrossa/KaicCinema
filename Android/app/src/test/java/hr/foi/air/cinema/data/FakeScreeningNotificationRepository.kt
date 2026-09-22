package hr.foi.air.cinema.data

class FakeScreeningNotificationRepository(
    private val publishResult: (String, String) -> Result<ScreeningNotification> = { screeningId, message ->
        Result.success(ScreeningNotification(screeningId = screeningId, message = message))
    },
) : ScreeningNotificationRepository {

    var publishNotificationCallCount = 0
        private set
    var lastScreeningId: String? = null
        private set
    var lastMessage: String? = null
        private set

    override suspend fun publishNotification(screeningId: String, message: String): Result<ScreeningNotification> {
        publishNotificationCallCount++
        lastScreeningId = screeningId
        lastMessage = message
        return publishResult(screeningId, message)
    }
}
