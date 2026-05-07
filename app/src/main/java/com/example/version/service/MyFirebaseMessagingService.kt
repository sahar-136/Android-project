package com.example.version.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.version.MainActivity
import com.example.version.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "SnapQuest_Notifications"
        private const val TAG = "FCM_Service"
    }

    // ✅ WHEN FCM TOKEN GENERATED/REFRESHED
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")

        // ✅ Try saving (works only if user logged in)
        saveFcmTokenToFirebase(token)
    }

    // ✅ WHEN PUSH NOTIFICATION RECEIVED
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // ✅ DATA-ONLY PAYLOAD SUPPORT (from backend Cloud Functions)
        // Backend will send: data { title, body, postId, type }
        val title = remoteMessage.data["title"] ?: (remoteMessage.notification?.title ?: "SnapQuest")
        val body = remoteMessage.data["body"] ?: (remoteMessage.notification?.body ?: "")
        val postId = remoteMessage.data["postId"] ?: ""
        val type = remoteMessage.data["type"] ?: "notification"

        Log.d(TAG, "Notification - Title: $title, Body: $body, PostId: $postId, Type: $type")

        // ✅ SHOW NOTIFICATION
        sendNotification(title, body, postId, type)
    }

    // ✅ SAVE FCM TOKEN TO FIRESTORE
    private fun saveFcmTokenToFirebase(token: String) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (!userId.isNullOrBlank()) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ FCM token saved to Firestore for user: $userId")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Failed to save FCM token: ${e.message}")
                    }
            } else {
                Log.d(TAG, "No user logged in, token not saved (will be saved after login in AuthViewModel)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in saveFcmTokenToFirebase: ${e.message}")
        }
    }

    // ✅ SHOW NOTIFICATION ON PHONE
    private fun sendNotification(title: String, body: String, postId: String, type: String) {
        try {
            createNotificationChannel()

            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("postId", postId)
                putExtra("notificationType", type)
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                this,
                postId.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // ✅ NOTE: Better to use a proper mono notification icon instead of launcher foreground
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(postId.hashCode(), notificationBuilder.build())

            Log.d(TAG, "✅ Notification displayed on phone")
        } catch (e: Exception) {
            Log.e(TAG, "Exception in sendNotification: ${e.message}")
        }
    }

    // ✅ CREATE NOTIFICATION CHANNEL (Android 8+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SnapQuest Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for likes and comments"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "✅ Notification channel created")
        }
    }
}