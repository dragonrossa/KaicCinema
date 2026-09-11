package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTicketRepository(
    private val reserveResult: Result<Reservation> = Result.success(Reservation()),
    private val reservationFlow: Flow<Reservation?> = flowOf(null),
    private val reservationsFlow: Flow<List<Reservation>> = flowOf(emptyList()),
    private val hasActiveReservationResult: Result<Boolean> = Result.success(false),
    private val updateReservationStatusResult: Result<Unit> = Result.success(Unit),
) : TicketRepository {

    var reserveTicketCallCount = 0
        private set
    var updateReservationStatusCallCount = 0
        private set
    var lastUpdatedReservationId: String? = null
        private set
    var lastUpdatedReservationStatus: ReservationStatus? = null
        private set

    override suspend fun reserveTicket(screeningId: String, userId: String): Result<Reservation> {
        reserveTicketCallCount++
        return reserveResult
    }

    override fun observeReservation(screeningId: String, userId: String): Flow<Reservation?> = reservationFlow

    override fun observeAllReservations(): Flow<List<Reservation>> = reservationsFlow

    override suspend fun hasActiveReservationForScreening(screeningId: String): Result<Boolean> = hasActiveReservationResult

    override suspend fun updateReservationStatus(reservationId: String, status: ReservationStatus): Result<Unit> {
        updateReservationStatusCallCount++
        lastUpdatedReservationId = reservationId
        lastUpdatedReservationStatus = status
        return updateReservationStatusResult
    }
}
