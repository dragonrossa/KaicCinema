package hr.foi.air.cinema.data

class FakeTicketRepository(
    private val reserveResult: Result<Reservation> = Result.success(Reservation()),
) : TicketRepository {

    var reserveTicketCallCount = 0
        private set

    override suspend fun reserveTicket(screeningId: String, userId: String): Result<Reservation> {
        reserveTicketCallCount++
        return reserveResult
    }
}
