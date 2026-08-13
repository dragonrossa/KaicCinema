package hr.foi.air.cinema.data

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    fun currentUserId(): String?
    fun logout()
}
