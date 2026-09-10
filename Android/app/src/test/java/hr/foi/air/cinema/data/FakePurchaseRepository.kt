package hr.foi.air.cinema.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePurchaseRepository(
    private val purchaseResult: Result<Purchase> = Result.success(Purchase()),
    private val purchasesFlow: Flow<List<Purchase>> = flowOf(emptyList()),
    private val hasPurchaseResult: Result<Boolean> = Result.success(false),
) : PurchaseRepository {

    var purchaseTicketCallCount = 0
        private set

    override suspend fun purchaseTicket(screeningId: String, userId: String): Result<Purchase> {
        purchaseTicketCallCount++
        return purchaseResult
    }

    override fun observeAllPurchases(): Flow<List<Purchase>> = purchasesFlow

    override suspend fun hasPurchaseForScreening(screeningId: String): Result<Boolean> = hasPurchaseResult
}
