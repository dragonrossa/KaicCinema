package hr.foi.air.cinema.logingoogle

import hr.foi.air.cinema.core.UserRole
import hr.foi.air.cinema.core.UserRoleResolver

class FakeUserRoleResolver(
    private val roleResult: Result<UserRole> = Result.success(UserRole.USER),
) : UserRoleResolver {
    override suspend fun resolveRole(uid: String): Result<UserRole> = roleResult
}
