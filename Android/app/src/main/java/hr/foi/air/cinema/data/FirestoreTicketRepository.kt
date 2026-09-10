package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val SCREENINGS_COLLECTION = "screenings"
private const val RESERVATIONS_COLLECTION = "reservations"
private const val FIELD_SCREENING_ID = "screeningId"
private const val FIELD_USER_ID = "userId"
private const val FIELD_RESERVED_SEATS = "reservedSeats"

class FirestoreTicketRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : TicketRepository {

    override suspend fun reserveTicket(screeningId: String, userId: String): Result<Reservation> = runCatching {
        val screeningRef = firestore.collection(SCREENINGS_COLLECTION).document(screeningId)
        val reservationRef = firestore.collection(RESERVATIONS_COLLECTION).document()

        firestore.runTransaction { transaction ->
            val screening = transaction.get(screeningRef).toObject(Screening::class.java)
                ?: throw IllegalStateException("Projekcija ne postoji")

            if (!screening.hasAvailableSeat()) {
                throw NoSeatsAvailableException()
            }

            val reservation = Reservation(
                id = reservationRef.id,
                screeningId = screeningId,
                userId = userId,
            )
            transaction.update(screeningRef, FIELD_RESERVED_SEATS, screening.reservedSeats + 1)
            transaction.set(reservationRef, reservation)
            reservation
        }.await()
    }

    override fun observeReservation(screeningId: String, userId: String): Flow<Reservation?> = callbackFlow {
        val registration = firestore.collection(RESERVATIONS_COLLECTION)
            .whereEqualTo(FIELD_SCREENING_ID, screeningId)
            .whereEqualTo(FIELD_USER_ID, userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reservation = snapshot?.documents?.firstOrNull()?.let { document ->
                    document.toObject(Reservation::class.java)?.copy(id = document.id)
                }
                trySend(reservation)
            }
        awaitClose { registration.remove() }
    }

    override fun observeAllReservations(): Flow<List<Reservation>> = callbackFlow {
        val registration = firestore.collection(RESERVATIONS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reservations = snapshot?.documents?.map { document ->
                    document.toObject(Reservation::class.java)?.copy(id = document.id) ?: Reservation(id = document.id)
                } ?: emptyList()
                trySend(reservations)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun hasActiveReservationForScreening(screeningId: String): Result<Boolean> = runCatching {
        val snapshot = firestore.collection(RESERVATIONS_COLLECTION)
            .whereEqualTo(FIELD_SCREENING_ID, screeningId)
            .get()
            .await()
        snapshot.documents.any { document ->
            when (document.toObject(Reservation::class.java)?.status) {
                ReservationStatus.PENDING, ReservationStatus.APPROVED -> true
                else -> false
            }
        }
    }
}
