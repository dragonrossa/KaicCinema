package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    suspend fun reserveTicket(screeningId: String, userId: String): Result<Reservation>
    fun observeReservation(screeningId: String, userId: String): Flow<Reservation?>
}
