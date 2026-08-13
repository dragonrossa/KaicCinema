package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeScreeningRepository(
    private val screeningsFlow: Flow<List<Screening>> = flowOf(emptyList()),
    private val screeningFlow: Flow<Screening?> = flowOf(null),
) : ScreeningRepository {

    override fun observeScreenings(): Flow<List<Screening>> = screeningsFlow

    override fun observeScreening(id: String): Flow<Screening?> = screeningFlow
}
