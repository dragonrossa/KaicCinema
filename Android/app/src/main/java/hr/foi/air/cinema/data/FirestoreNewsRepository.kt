package hr.foi.air.cinema.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val NEWS_COLLECTION = "news"
private const val FIELD_CREATED_AT = "createdAt"

class FirestoreNewsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : NewsRepository {

    override fun observeNews(): Flow<List<News>> = callbackFlow {
        val registration = firestore.collection(NEWS_COLLECTION)
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val news = snapshot?.documents?.map { document ->
                    document.toObject(News::class.java)?.copy(id = document.id) ?: News(id = document.id)
                } ?: emptyList()
                trySend(news)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addNews(news: News): Result<News> = runCatching {
        val newsRef = firestore.collection(NEWS_COLLECTION).document()
        val newsToSave = news.copy(id = newsRef.id)
        newsRef.set(newsToSave).await()
        newsToSave
    }
}
