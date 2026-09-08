package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeScreeningRepository(
    private val screeningsFlow: Flow<List<Screening>> = flowOf(emptyList()),
    private val screeningFlow: Flow<Screening?> = flowOf(null),
    private val addScreeningResult: (Screening) -> Result<Screening> = { Result.success(it) },
    private val deleteScreeningResult: (String) -> Result<Unit> = { Result.success(Unit) },
) : ScreeningRepository {

    var addScreeningCallCount = 0
        private set
    var lastAddedScreening: Screening? = null
        private set
    var deleteScreeningCallCount = 0
        private set
    var lastDeletedScreeningId: String? = null
        private set

    override fun observeScreenings(): Flow<List<Screening>> = screeningsFlow

    override fun observeScreening(id: String): Flow<Screening?> = screeningFlow

    override suspend fun addScreening(screening: Screening): Result<Screening> {
        addScreeningCallCount++
        lastAddedScreening = screening
        return addScreeningResult(screening)
    }

    override suspend fun deleteScreening(id: String): Result<Unit> {
        deleteScreeningCallCount++
        lastDeletedScreeningId = id
        return deleteScreeningResult(id)
    }
}
