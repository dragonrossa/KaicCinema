package hr.foi.air.cinema.seed

import com.google.cloud.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

data class SeedScreening(
    val movieTitle: String,
    val description: String,
    val category: String,
    val views: Long,
    val popularity: Long,
    val totalSeats: Long,
    val reservedSeats: Long,
    val daysFromNow: Long,
)

val SAMPLE_SCREENINGS = listOf(
    SeedScreening("Dune: Part Three", "Nastavak epske svemirske sage.", "Znanstvena fantastika", 1500, 92, 120, 45, 1),
    SeedScreening("Fantastična gospoda Fox", "Animirana obiteljska avantura.", "Animirani", 800, 76, 80, 12, 2),
    SeedScreening("Posljednji lovac", "Akcijski triler s obratima.", "Akcija", 2300, 88, 150, 150, 1),
    SeedScreening("Ljeto u Splitu", "Romantična komedija na Jadranu.", "Komedija", 640, 55, 100, 30, 3),
    SeedScreening("Tama ispod grada", "Mračni psihološki horor.", "Horor", 990, 81, 90, 60, 4),
    SeedScreening("Zvijezde nad Zagrebom", "Domaća drama o odrastanju.", "Drama", 410, 64, 70, 10, 5),
)

fun SeedScreening.toFirestoreData(now: Instant): Map<String, Any> {
    val screeningTime = now.plus(daysFromNow, ChronoUnit.DAYS)
    return mapOf(
        "movieTitle" to movieTitle,
        "description" to description,
        "screeningTime" to Timestamp.ofTimeSecondsAndNanos(screeningTime.epochSecond, screeningTime.nano),
        "category" to category,
        "views" to views,
        "popularity" to popularity,
        "totalSeats" to totalSeats,
        "reservedSeats" to reservedSeats,
    )
}

fun shouldClearExisting(args: Array<String>): Boolean = args.contains("--clear")

fun resolveCredentialsPath(env: Map<String, String>): String =
    env["GOOGLE_APPLICATION_CREDENTIALS"]
        ?: error(
            "Nedostaje GOOGLE_APPLICATION_CREDENTIALS environment varijabla. " +
                "Postavi je na putanju do service account JSON ključa (vidi README).",
        )