package hr.foi.air.cinema.logingoogle

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

private const val NEW_SCREENINGS_TOPIC = "new_screenings"
private const val SCREENING_NOTIFICATIONS_TOPIC = "screening_notifications"
private const val USERS_COLLECTION = "users"
private const val FIELD_FCM_TOKEN = "fcmToken"

interface FcmTokenProvider {
    suspend fun getToken(): Result<String>
    suspend fun saveToken(uid: String, token: String): Result<Unit>
    suspend fun subscribeToNewScreenings(): Result<Unit>
    suspend fun subscribeToScreeningNotifications(): Result<Unit>
}

class FirebaseFcmTokenProvider(
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : FcmTokenProvider {

    override suspend fun getToken(): Result<String> = runCatching {
        messaging.token.await()
    }

    override suspend fun saveToken(uid: String, token: String): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION).document(uid)
            .set(mapOf(FIELD_FCM_TOKEN to token), SetOptions.merge())
            .await()
    }

    override suspend fun subscribeToNewScreenings(): Result<Unit> = runCatching {
        messaging.subscribeToTopic(NEW_SCREENINGS_TOPIC).await()
    }

    override suspend fun subscribeToScreeningNotifications(): Result<Unit> = runCatching {
        messaging.subscribeToTopic(SCREENING_NOTIFICATIONS_TOPIC).await()
    }
}
