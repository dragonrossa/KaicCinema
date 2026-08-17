package hr.foi.air.cinema.data

import com.google.firebase.Timestamp

data class Purchase(
    val id: String = "",
    val screeningId: String = "",
    val userId: String = "",
    val status: PurchaseStatus = PurchaseStatus.COMPLETED,
    val createdAt: Timestamp = Timestamp.now(),
)
