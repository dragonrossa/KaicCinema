package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"
private const val FIELD_ROLE = "role"
private const val FIELD_FCM_TOKEN = "fcmToken"
private const val ROLE_ADMIN = "admin"

class FirestoreUserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : UserRepository {

    override suspend fun getUserRole(uid: String): Result<UserRole> = runCatching {
        val document = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        if (document.getString(FIELD_ROLE) == ROLE_ADMIN) UserRole.ADMIN else UserRole.USER
    }

    override suspend fun updateFcmToken(uid: String, token: String): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION).document(uid)
            .set(mapOf(FIELD_FCM_TOKEN to token), SetOptions.merge())
            .await()
    }
}
