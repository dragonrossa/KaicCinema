package hr.foi.air.cinema.data

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import hr.foi.air.cinema.core.AnalyticsLogger

class FirebaseAnalyticsLogger(
    private val analytics: FirebaseAnalytics = Firebase.analytics,
) : AnalyticsLogger {

    override fun logEvent(name: String, params: Map<String, String>) {
        val bundle = Bundle()
        params.forEach { (key, value) -> bundle.putString(key, value) }
        analytics.logEvent(name, bundle)
    }
}
