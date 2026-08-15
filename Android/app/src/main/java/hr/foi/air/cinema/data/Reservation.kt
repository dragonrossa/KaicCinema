package hr.foi.air.cinema.data

import com.google.firebase.Timestamp

data class Reservation(
    val id: String = "",
    val screeningId: String = "",
    val userId: String = "",
    val status: ReservationStatus = ReservationStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
)
