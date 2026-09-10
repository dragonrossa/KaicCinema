package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val SCREENINGS_COLLECTION = "screenings"
private const val PURCHASES_COLLECTION = "purchases"
private const val FIELD_RESERVED_SEATS = "reservedSeats"
private const val FIELD_SCREENING_ID = "screeningId"

class FirestorePurchaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : PurchaseRepository {

    override suspend fun purchaseTicket(screeningId: String, userId: String): Result<Purchase> = runCatching {
        val screeningRef = firestore.collection(SCREENINGS_COLLECTION).document(screeningId)
        val purchaseRef = firestore.collection(PURCHASES_COLLECTION).document()

        firestore.runTransaction { transaction ->
            val screening = transaction.get(screeningRef).toObject(Screening::class.java)
                ?: throw IllegalStateException("Projekcija ne postoji")

            if (!screening.hasAvailableSeat()) {
                throw NoSeatsAvailableException()
            }

            val purchase = Purchase(
                id = purchaseRef.id,
                screeningId = screeningId,
                userId = userId,
            )
            transaction.update(screeningRef, FIELD_RESERVED_SEATS, screening.reservedSeats + 1)
            transaction.set(purchaseRef, purchase)
            purchase
        }.await()
    }

    override fun observeAllPurchases(): Flow<List<Purchase>> = callbackFlow {
        val registration = firestore.collection(PURCHASES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val purchases = snapshot?.documents?.map { document ->
                    document.toObject(Purchase::class.java)?.copy(id = document.id) ?: Purchase(id = document.id)
                } ?: emptyList()
                trySend(purchases)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun hasPurchaseForScreening(screeningId: String): Result<Boolean> = runCatching {
        val snapshot = firestore.collection(PURCHASES_COLLECTION)
            .whereEqualTo(FIELD_SCREENING_ID, screeningId)
            .limit(1)
            .get()
            .await()
        !snapshot.isEmpty
    }
}
