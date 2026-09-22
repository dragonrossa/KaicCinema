package hr.foi.air.cinema.data

import com.google.firebase.Timestamp

data class ScreeningNotification(
    val id: String = "",
    val screeningId: String = "",
    val message: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)
