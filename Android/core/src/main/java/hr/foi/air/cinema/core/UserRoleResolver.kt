package hr.foi.air.cinema.core

interface UserRoleResolver {
    suspend fun resolveRole(uid: String): Result<UserRole>
}
