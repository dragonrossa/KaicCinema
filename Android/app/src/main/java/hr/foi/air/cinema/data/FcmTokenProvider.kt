package hr.foi.air.cinema.data

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

const val NEW_SCREENINGS_TOPIC = "new_screenings"

interface FcmTokenProvider {
    suspend fun getToken(): Result<String>
    suspend fun subscribeToNewScreenings(): Result<Unit>
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
}
