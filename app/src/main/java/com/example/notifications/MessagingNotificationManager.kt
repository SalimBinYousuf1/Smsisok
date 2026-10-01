package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.MainActivity

object MessagingNotificationManager {

    const val CHANNEL_ID = "salim_sms_messages"
    const val CHANNEL_NAME = "Messages"
    const val KEY_TEXT_REPLY = "key_text_reply"
    const val ACTION_REPLY = "com.example.salim.ACTION_REPLY"
    const val ACTION_MARK_READ = "com.example.salim.ACTION_MARK_READ"
    const val EXTRA_ADDRESS = "extra_address"
    const val EXTRA_THREAD_ID = "extra_thread_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming SMS messages"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun showIncomingMessageNotification(
        context: Context,
        sender: String,
        body: String,
        threadId: Long = 0L,
        senderDisplayName: String? = null
    ) {
        initNotificationChannel(context)

        val notificationId = (sender.hashCode() xor threadId.hashCode().toInt()).let { if (it < 0) -it else it }

        // Content intent: open conversation in MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("thread_id", threadId)
            putExtra("address", sender)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Direct Reply RemoteInput Action
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Reply")
            .build()

        val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra(EXTRA_ADDRESS, sender)
            putExtra(EXTRA_THREAD_ID, threadId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 100,
            replyIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Mark as Read Action
        val markReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_READ
            putExtra(EXTRA_THREAD_ID, threadId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 200,
            markReadIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val markReadAction = NotificationCompat.Action.Builder(
            android.R.drawable.checkbox_on_background,
            "Mark as read",
            markReadPendingIntent
        ).build()

        val title = senderDisplayName ?: sender

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .addAction(replyAction)
            .addAction(markReadAction)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }
}
