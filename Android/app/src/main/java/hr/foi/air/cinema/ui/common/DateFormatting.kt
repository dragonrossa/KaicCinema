package hr.foi.air.cinema.ui.common

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

fun formatScreeningTime(timestamp: Timestamp): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.forLanguageTag("hr-HR"))
    return formatter.format(timestamp.toDate())
}
