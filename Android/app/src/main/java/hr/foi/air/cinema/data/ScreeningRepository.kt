package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow

interface ScreeningRepository {
    fun observeScreenings(): Flow<List<Screening>>
    fun observeScreening(id: String): Flow<Screening?>
    suspend fun addScreening(screening: Screening): Result<Screening>
}
