package hr.foi.air.cinema.data

class FakePurchaseRepository(
    private val purchaseResult: Result<Purchase> = Result.success(Purchase()),
) : PurchaseRepository {

    var purchaseTicketCallCount = 0
        private set

    override suspend fun purchaseTicket(screeningId: String, userId: String): Result<Purchase> {
        purchaseTicketCallCount++
        return purchaseResult
    }
}
