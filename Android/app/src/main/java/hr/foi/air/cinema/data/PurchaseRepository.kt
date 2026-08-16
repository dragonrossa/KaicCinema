package hr.foi.air.cinema.data

interface PurchaseRepository {
    suspend fun purchaseTicket(screeningId: String, userId: String): Result<Purchase>
}
