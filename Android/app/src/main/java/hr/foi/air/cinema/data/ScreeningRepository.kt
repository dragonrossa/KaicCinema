package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow

interface ScreeningRepository {
    fun observeScreenings(): Flow<List<Screening>>
}
