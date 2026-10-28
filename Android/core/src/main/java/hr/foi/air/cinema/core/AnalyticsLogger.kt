package hr.foi.air.cinema.core

object AnalyticsEvents {
    const val LOGIN = "login"
    const val LOGOUT = "logout"
    const val SCREENING_VIEWED = "screening_viewed"
    const val RESERVATION_CREATED = "reservation_created"
    const val PURCHASE_COMPLETED = "purchase_completed"
}

object AnalyticsParams {
    const val SCREENING_ID = "screening_id"
}

interface AnalyticsLogger {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
}
