package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    suspend fun purchaseTicket(screeningId: String, userId: String): Result<Purchase>
    fun observeAllPurchases(): Flow<List<Purchase>>
}
