package hr.foi.air.cinema.data

import com.google.firebase.Timestamp

data class Purchase(
    val id: String = "",
    val screeningId: String = "",
    val userId: String = "",
    val purchasedAt: Timestamp = Timestamp.now(),
)
