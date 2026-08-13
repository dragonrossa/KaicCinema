package hr.foi.air.cinema.data

interface UserRepository {
    suspend fun getUserRole(uid: String): Result<UserRole>
}
