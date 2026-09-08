package hr.foi.air.cinema.data

import com.google.firebase.Timestamp

data class News(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)
