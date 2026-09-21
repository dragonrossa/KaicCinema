package hr.foi.air.cinema.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import hr.foi.air.cinema.MainActivity
import hr.foi.air.cinema.R
import hr.foi.air.cinema.data.FirebaseAuthRepository
import hr.foi.air.cinema.data.FirestoreUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val CINEMA_NOTIFICATIONS_CHANNEL_ID = "cinema_notifications"

class CinemaMessagingService : FirebaseMessagingService() {

    private val authRepository = FirebaseAuthRepository()
    private val userRepository = FirestoreUserRepository()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = authRepository.currentUserId() ?: return
        serviceScope.launch { userRepository.updateFcmToken(uid, token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: return
        val body = message.notification?.body.orEmpty()
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        ensureNotificationChannel()
        val notification = NotificationCompat.Builder(this, CINEMA_NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent())
            .build()

        ContextCompat.getSystemService(this, NotificationManager::class.java)
            ?.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CINEMA_NOTIFICATIONS_CHANNEL_ID,
            getString(R.string.cinema_notifications_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        )
        ContextCompat.getSystemService(this, NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }
}
