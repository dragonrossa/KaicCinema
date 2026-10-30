package hr.foi.air.cinema.logingoogle

interface GoogleAuthRepository {
    suspend fun signInWithIdToken(idToken: String): Result<Unit>
    fun currentUserId(): String?
}
