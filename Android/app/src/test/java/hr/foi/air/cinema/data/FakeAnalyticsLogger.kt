package hr.foi.air.cinema.data

class FakeAnalyticsLogger : AnalyticsLogger {

    val loggedEvents = mutableListOf<Pair<String, Map<String, String>>>()

    override fun logEvent(name: String, params: Map<String, String>) {
        loggedEvents.add(name to params)
    }
}
