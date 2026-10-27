package hr.foi.air.cinema.data

import hr.foi.air.cinema.core.UserRole

interface UserRepository {
    suspend fun getUserRole(uid: String): Result<UserRole>
    suspend fun updateFcmToken(uid: String, token: String): Result<Unit>
}
