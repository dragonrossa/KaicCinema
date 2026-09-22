package hr.foi.air.cinema.data

interface ScreeningNotificationRepository {
    suspend fun publishNotification(screeningId: String, message: String): Result<ScreeningNotification>
}
