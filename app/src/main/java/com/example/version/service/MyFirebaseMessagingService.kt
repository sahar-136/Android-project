package com.example.version.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.version.MainActivity
import com.example.version.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "SnapQuest_Notifications"
        private const val TAG = "FCM_Service"
    }

    // ✅ WHEN FCM TOKEN GENERATED/REFRESHED
    // Called when app first installs or when FCM token refreshes
    // This is called automatically by Firebase
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")

        // ✅ SAVE TO FIRESTORE (User collection mein)
        // Jab naya token mile to Firebase mein save kar do
        saveFcmTokenToFirebase(token)
    }

    // ✅ WHEN PUSH NOTIFICATION RECEIVED
    // Called when push notification aata hai (app background mein ho ya foreground)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // ✅ GET NOTIFICATION DATA
        // Server se jo data ata hai us ko extract karte hain
        val title = remoteMessage.notification?.title ?: "SnapQuest"
        val body = remoteMessage.notification?.body ?: ""
        val postId = remoteMessage.data["postId"] ?: ""
        val type = remoteMessage.data["type"] ?: "notification"

        Log.d(TAG, "Notification - Title: $title, Body: $body, PostId: $postId, Type: $type")

        // ✅ SHOW NOTIFICATION ON PHONE
        // Phone ke top bar par notification dikhe
        sendNotification(title, body, postId, type)
    }

    // ✅ SAVE FCM TOKEN TO FIRESTORE
    // Function jo FCM token ko Firebase mein save karta hai
    private fun saveFcmTokenToFirebase(token: String) {
        try {
            // ✅ GET CURRENT USER ID
            // Firebase Auth se current user ka ID lo
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

            // Agar user logged in hai to token save kar
            if (userId != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
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
                Log.d(TAG, "No user logged in, token not saved")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in saveFcmTokenToFirebase: ${e.message}")
        }
    }

    // ✅ SHOW NOTIFICATION ON PHONE (WhatsApp style)
    // Jab notification aaye to phone ke top bar par dikhe
    private fun sendNotification(title: String, body: String, postId: String, type: String) {
        try {
            // ✅ CREATE NOTIFICATION CHANNEL (Android 8+)
            createNotificationChannel()

            // ✅ CREATE INTENT FOR DEEP LINKING
            // Jab notification click hoga to MainActivity khulega
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                // ✅ PASS POST ID
                // Jo post like/comment hui, usi ke details page par jaao
                putExtra("postId", postId)
                putExtra("notificationType", type)
            }

            // ✅ CREATE PENDING INTENT
            // Notification click karne par ye intent execute hoga
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                this, postId.hashCode(), intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // ✅ BUILD NOTIFICATION
            // Notification ko design karte hain
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // ← App icon
                .setContentTitle(title)  // ← Title (User ka name + action)
                .setContentText(body)  // ← Body text
                .setAutoCancel(true)  // ← Notification dismiss ho jaye jab click ho
                .setContentIntent(pendingIntent)  // ← Click karne par kya ho
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // ← High priority (sound + vibration)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))  // ← Large text display

            // ✅ SHOW NOTIFICATION
            // Android notification manager se notification dikha do
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(postId.hashCode(), notificationBuilder.build())

            Log.d(TAG, "✅ Notification displayed on phone")
        } catch (e: Exception) {
            Log.e(TAG, "Exception in sendNotification: ${e.message}")
        }
    }

    // ✅ CREATE NOTIFICATION CHANNEL (Android 8+)
    // Android 8 se channel banana zaroori hai notification dikhaane ke liye
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ✅ CREATE CHANNEL
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SnapQuest Notifications",
                NotificationManager.IMPORTANCE_HIGH  // High priority
            ).apply {
                description = "Notifications for likes and comments"
                enableLights(true)  // ← Light on (LED)
                enableVibration(true)  // ← Vibration on
            }

            // ✅ REGISTER CHANNEL
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "✅ Notification channel created")
        }
    }
}