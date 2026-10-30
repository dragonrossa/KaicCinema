package hr.foi.air.cinema.logingoogle

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class FirebaseGoogleAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : GoogleAuthRepository {

    override suspend fun signInWithIdToken(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        Unit
    }

    override fun currentUserId(): String? = auth.currentUser?.uid
}
