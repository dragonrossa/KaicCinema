package hr.foi.air.cinema.data

class FakeUserRepository(
    private val roleResult: Result<UserRole> = Result.success(UserRole.USER),
) : UserRepository {

    override suspend fun getUserRole(uid: String): Result<UserRole> = roleResult
}
