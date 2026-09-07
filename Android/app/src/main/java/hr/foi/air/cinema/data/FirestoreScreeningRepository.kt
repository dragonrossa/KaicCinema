package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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

    override fun observeScreening(id: String): Flow<Screening?> = callbackFlow {
        val registration = firestore.collection(SCREENINGS_COLLECTION).document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val screening = snapshot?.takeIf { it.exists() }
                    ?.toObject(Screening::class.java)
                    ?.copy(id = snapshot.id)
                trySend(screening)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addScreening(screening: Screening): Result<Screening> = runCatching {
        val screeningRef = firestore.collection(SCREENINGS_COLLECTION).document()
        val screeningToSave = screening.copy(id = screeningRef.id)
        screeningRef.set(screeningToSave).await()
        screeningToSave
    }
}
