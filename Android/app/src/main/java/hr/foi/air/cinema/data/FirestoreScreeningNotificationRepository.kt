package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val SCREENING_NOTIFICATIONS_COLLECTION = "screeningNotifications"

class FirestoreScreeningNotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ScreeningNotificationRepository {

    override suspend fun publishNotification(screeningId: String, message: String): Result<ScreeningNotification> = runCatching {
        val notificationRef = firestore.collection(SCREENING_NOTIFICATIONS_COLLECTION).document()
        val notification = ScreeningNotification(
            id = notificationRef.id,
            screeningId = screeningId,
            message = message,
        )
        notificationRef.set(notification).await()
        notification
    }
}
