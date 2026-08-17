package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val RESERVATIONS_COLLECTION = "reservations"
private const val FIELD_SCREENING_ID = "screeningId"
private const val FIELD_USER_ID = "userId"

class FirestoreTicketRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : TicketRepository {

    override suspend fun reserveTicket(screeningId: String, userId: String): Result<Reservation> = runCatching {
        val documentRef = firestore.collection(RESERVATIONS_COLLECTION).document()
        val reservation = Reservation(
            id = documentRef.id,
            screeningId = screeningId,
            userId = userId,
        )
        documentRef.set(reservation).await()
        reservation
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
}
