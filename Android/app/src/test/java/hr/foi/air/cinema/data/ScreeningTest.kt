package hr.foi.air.cinema.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreeningTest {

    @Test
    fun availableSeats_isTotalMinusReserved() {
        val screening = Screening(totalSeats = 100, reservedSeats = 40)

        assertEquals(60, screening.availableSeats)
    }

    @Test
    fun hasAvailableSeat_trueWhenSeatsRemain() {
        val screening = Screening(totalSeats = 100, reservedSeats = 99)

        assertTrue(screening.hasAvailableSeat())
    }

    @Test
    fun hasAvailableSeat_falseWhenSoldOut() {
        val screening = Screening(totalSeats = 100, reservedSeats = 100)

        assertFalse(screening.hasAvailableSeat())
    }

    @Test
    fun hasAvailableSeat_falseWhenOverCapacity() {
        val screening = Screening(totalSeats = 100, reservedSeats = 101)

        assertFalse(screening.hasAvailableSeat())
    }
}
