package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTicketRepository(
    private val reserveResult: Result<Reservation> = Result.success(Reservation()),
    private val reservationFlow: Flow<Reservation?> = flowOf(null),
    private val reservationsFlow: Flow<List<Reservation>> = flowOf(emptyList()),
    private val hasActiveReservationResult: Result<Boolean> = Result.success(false),
) : TicketRepository {

    var reserveTicketCallCount = 0
        private set

    override suspend fun reserveTicket(screeningId: String, userId: String): Result<Reservation> {
        reserveTicketCallCount++
        return reserveResult
    }

    override fun observeReservation(screeningId: String, userId: String): Flow<Reservation?> = reservationFlow

    override fun observeAllReservations(): Flow<List<Reservation>> = reservationsFlow

    override suspend fun hasActiveReservationForScreening(screeningId: String): Result<Boolean> = hasActiveReservationResult
}
