package hr.foi.air.cinema.data

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

interface FcmTokenProvider {
    suspend fun getToken(): Result<String>
}

class FirebaseFcmTokenProvider(
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
) : FcmTokenProvider {

    override suspend fun getToken(): Result<String> = runCatching {
        messaging.token.await()
    }
}
