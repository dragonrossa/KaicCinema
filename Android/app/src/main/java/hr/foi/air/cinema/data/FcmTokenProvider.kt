package hr.foi.air.cinema.data

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

const val NEW_SCREENINGS_TOPIC = "new_screenings"
const val SCREENING_NOTIFICATIONS_TOPIC = "screening_notifications"

interface FcmTokenProvider {
    suspend fun getToken(): Result<String>
    suspend fun subscribeToNewScreenings(): Result<Unit>
    suspend fun subscribeToScreeningNotifications(): Result<Unit>
}

class FirebaseFcmTokenProvider(
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
) : FcmTokenProvider {

    override suspend fun getToken(): Result<String> = runCatching {
        messaging.token.await()
    }

    override suspend fun subscribeToNewScreenings(): Result<Unit> = runCatching {
        messaging.subscribeToTopic(NEW_SCREENINGS_TOPIC).await()
    }

    override suspend fun subscribeToScreeningNotifications(): Result<Unit> = runCatching {
        messaging.subscribeToTopic(SCREENING_NOTIFICATIONS_TOPIC).await()
    }
}
