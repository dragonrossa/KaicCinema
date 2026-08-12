package hr.foi.air.cinema.data

import com.google.firebase.Timestamp

data class Screening(
    val id: String = "",
    val movieTitle: String = "",
    val screeningTime: Timestamp = Timestamp.now(),
    val category: String = "",
    val views: Long = 0,
    val popularity: Long = 0,
)
