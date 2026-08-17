package hr.foi.air.cinema.seed

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.io.FileInputStream
import java.time.Instant

private const val SCREENINGS_COLLECTION = "screenings"

fun main(args: Array<String>) {
    val credentialsPath = resolveCredentialsPath(System.getenv())

    val credentials = FileInputStream(credentialsPath).use { GoogleCredentials.fromStream(it) }
    val options = FirebaseOptions.builder()
        .setCredentials(credentials)
        .build()
    FirebaseApp.initializeApp(options)

    val firestore = FirestoreClient.getFirestore()
    val screeningsCollection = firestore.collection(SCREENINGS_COLLECTION)

    if (shouldClearExisting(args)) {
        val existing = screeningsCollection.get().get()
        existing.documents.forEach { it.reference.delete().get() }
        println("Obrisano ${existing.size()} postojećih dokumenata iz '$SCREENINGS_COLLECTION'.")
    }

    val now = Instant.now()
    SAMPLE_SCREENINGS.forEach { sample ->
        screeningsCollection.document().set(sample.toFirestoreData(now)).get()
    }

    println("Dodano ${SAMPLE_SCREENINGS.size} test projekcija u '$SCREENINGS_COLLECTION' kolekciju.")
}