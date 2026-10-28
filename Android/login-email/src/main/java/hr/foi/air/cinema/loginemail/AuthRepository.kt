package hr.foi.air.cinema.loginemail

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    fun currentUserId(): String?
    fun logout()
}
