package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val SCREENINGS_COLLECTION = "screenings"

class FirestoreScreeningRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ScreeningRepository {

    override fun observeScreenings(): Flow<List<Screening>> = callbackFlow {
        val registration = firestore.collection(SCREENINGS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val screenings = snapshot?.documents?.map { document ->
                    document.toObject(Screening::class.java)?.copy(id = document.id) ?: Screening(id = document.id)
                } ?: emptyList()
                trySend(screenings)
            }
        awaitClose { registration.remove() }
    }
}
