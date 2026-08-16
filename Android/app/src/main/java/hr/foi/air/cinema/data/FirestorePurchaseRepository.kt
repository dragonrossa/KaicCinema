package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
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
}
