package hr.foi.air.cinema.seed

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedDataTest {

    @Test
    fun sampleScreenings_hasSixEntries() {
        assertEquals(6, SAMPLE_SCREENINGS.size)
    }

    @Test
    fun sampleScreenings_hasVaryingCategories() {
        val categories = SAMPLE_SCREENINGS.map { it.category }.toSet()

        assertTrue("Expected more than one distinct category, got $categories", categories.size > 1)
    }

    @Test
    fun sampleScreenings_hasVaryingViewsPopularityAndSeats() {
        assertTrue(SAMPLE_SCREENINGS.map { it.views }.toSet().size > 1)
        assertTrue(SAMPLE_SCREENINGS.map { it.popularity }.toSet().size > 1)
        assertTrue(SAMPLE_SCREENINGS.map { it.totalSeats }.toSet().size > 1)
    }

    @Test
    fun toFirestoreData_mapsAllRequiredFields() {
        val sample = SeedScreening(
            movieTitle = "Test film",
            description = "Opis",
            category = "Drama",
            views = 100,
            popularity = 50,
            totalSeats = 80,
            reservedSeats = 20,
            daysFromNow = 2,
        )

        val data = sample.toFirestoreData(Instant.now())

        assertEquals("Test film", data["movieTitle"])
        assertEquals("Opis", data["description"])
        assertEquals("Drama", data["category"])
        assertEquals(100L, data["views"])
        assertEquals(50L, data["popularity"])
        assertEquals(80L, data["totalSeats"])
        assertEquals(20L, data["reservedSeats"])
        assertTrue(data.containsKey("screeningTime"))
    }

    @Test
    fun toFirestoreData_screeningTimeIsInTheFuture() {
        val now = Instant.now()
        val sample = SAMPLE_SCREENINGS.first()

        val data = sample.toFirestoreData(now)
        val screeningTime = data["screeningTime"] as com.google.cloud.Timestamp

        assertTrue(screeningTime.toDate().toInstant().isAfter(now))
    }

    @Test
    fun shouldClearExisting_trueWhenClearFlagPresent() {
        assertTrue(shouldClearExisting(arrayOf("--clear")))
    }

    @Test
    fun shouldClearExisting_falseWhenNoArgs() {
        assertEquals(false, shouldClearExisting(emptyArray()))
    }

    @Test
    fun resolveCredentialsPath_returnsPathWhenEnvSet() {
        val path = resolveCredentialsPath(mapOf("GOOGLE_APPLICATION_CREDENTIALS" to "/tmp/key.json"))

        assertEquals("/tmp/key.json", path)
    }

    @Test
    fun resolveCredentialsPath_throwsWhenEnvMissing() {
        assertThrows(IllegalStateException::class.java) {
            resolveCredentialsPath(emptyMap())
        }
    }
}