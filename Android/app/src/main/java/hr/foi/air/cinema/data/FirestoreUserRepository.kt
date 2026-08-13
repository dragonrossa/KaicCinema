package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"
private const val FIELD_ROLE = "role"
private const val ROLE_ADMIN = "admin"

class FirestoreUserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : UserRepository {

    override suspend fun getUserRole(uid: String): Result<UserRole> = runCatching {
        val document = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        if (document.getString(FIELD_ROLE) == ROLE_ADMIN) UserRole.ADMIN else UserRole.USER
    }
}
