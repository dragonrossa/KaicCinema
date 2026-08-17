package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val PURCHASES_COLLECTION = "purchases"

class FirestorePurchaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : PurchaseRepository {

    override suspend fun purchaseTicket(screeningId: String, userId: String): Result<Purchase> = runCatching {
        val documentRef = firestore.collection(PURCHASES_COLLECTION).document()
        val purchase = Purchase(
            id = documentRef.id,
            screeningId = screeningId,
            userId = userId,
        )
        documentRef.set(purchase).await()
        purchase
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
}
