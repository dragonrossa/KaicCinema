package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTicketRepository(
    private val reserveResult: Result<Reservation> = Result.success(Reservation()),
    private val reservationFlow: Flow<Reservation?> = flowOf(null),
) : TicketRepository {

    var reserveTicketCallCount = 0
        private set

    override suspend fun reserveTicket(screeningId: String, userId: String): Result<Reservation> {
        reserveTicketCallCount++
        return reserveResult
    }

    override fun observeReservation(screeningId: String, userId: String): Flow<Reservation?> = reservationFlow
}
