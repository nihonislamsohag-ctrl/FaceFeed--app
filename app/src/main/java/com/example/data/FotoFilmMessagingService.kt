package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FotoFilmMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed")
        FirebaseManager.saveFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val type = data["type"] ?: "chat"
        val isCall = type == "call" || type == "AUDIO" || type == "VIDEO" || type == "GROUP"

        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: if (isCall) "📞 ইনকামিং কল • ChatFilm" else "💬 নতুন মেসেজ • FotoFilm"

        val body = remoteMessage.notification?.body
            ?: data["body"]
            ?: if (isCall) "আপনাকে একজন কল করছেন — রিসিভ করতে ট্যাপ করুন" else "নতুন আপডেট দেখতে ট্যাপ করুন"

        showSystemNotification(
            context = applicationContext,
            title = title,
            body = body,
            isCall = isCall
        )
    }

    companion object {
        private const val TAG = "FotoFilmFCM"
        const val CHANNEL_CALLS = "fotofilm_calls_channel"
        const val CHANNEL_MESSAGES = "fotofilm_messages_channel"

        fun ensureChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                val callChannel = NotificationChannel(
                    CHANNEL_CALLS,
                    "ChatFilm অডিও ও ভিডিও কল",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "ইনকামিং ChatFilm অডিও ও ভিডিও কল রিং অ্যালার্ট"
                    enableVibration(true)
                }

                val msgChannel = NotificationChannel(
                    CHANNEL_MESSAGES,
                    "ChatFilm মেসেজ ও নোটিফিকেশন",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "ChatFilm মেসেজ, লাইক, কমেন্ট ও অ্যাপ আপডেট"
                }

                nm.createNotificationChannel(callChannel)
                nm.createNotificationChannel(msgChannel)
            }
        }

        fun showSystemNotification(
            context: Context,
            title: String,
            body: String,
            isCall: Boolean = false
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }

            ensureChannels(context)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                (System.currentTimeMillis() % 10000).toInt(),
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(
                context,
                if (isCall) CHANNEL_CALLS else CHANNEL_MESSAGES
            )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(if (isCall) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            try {
                NotificationManagerCompat.from(context).notify(
                    (System.currentTimeMillis() % 100000).toInt(),
                    notification
                )
            } catch (_: SecurityException) {
            }
        }
    }
}
