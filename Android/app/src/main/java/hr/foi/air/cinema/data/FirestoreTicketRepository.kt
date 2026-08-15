package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val RESERVATIONS_COLLECTION = "reservations"

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
}
