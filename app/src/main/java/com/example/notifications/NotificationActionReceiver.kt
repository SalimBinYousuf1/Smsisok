package com.example.notifications

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.sms.SmsSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra(MessagingNotificationManager.EXTRA_NOTIFICATION_ID, 0)
        val threadId = intent.getLongExtra(MessagingNotificationManager.EXTRA_THREAD_ID, 0L)
        val address = intent.getStringExtra(MessagingNotificationManager.EXTRA_ADDRESS) ?: ""

        when (action) {
            MessagingNotificationManager.ACTION_REPLY -> {
                val bundle = RemoteInput.getResultsFromIntent(intent)
                val replyText = bundle?.getCharSequence(MessagingNotificationManager.KEY_TEXT_REPLY)?.toString()
                if (!replyText.isNullOrBlank() && address.isNotBlank()) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val sender = SmsSender(context)
                            sender.sendSms(address, replyText)
                            NotificationManagerCompat.from(context).cancel(notificationId)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            MessagingNotificationManager.ACTION_MARK_READ -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val values = ContentValues().apply {
                            put(Telephony.Sms.READ, 1)
                            put(Telephony.Sms.SEEN, 1)
                        }
                        context.contentResolver.update(
                            Telephony.Sms.Inbox.CONTENT_URI,
                            values,
                            "${Telephony.Sms.THREAD_ID} = ?",
                            arrayOf(threadId.toString())
                        )
                        NotificationManagerCompat.from(context).cancel(notificationId)
                    } catch (_: Exception) {
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
