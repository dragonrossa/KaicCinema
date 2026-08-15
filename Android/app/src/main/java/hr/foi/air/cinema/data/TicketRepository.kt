package hr.foi.air.cinema.data

interface TicketRepository {
    suspend fun reserveTicket(screeningId: String, userId: String): Result<Reservation>
}
