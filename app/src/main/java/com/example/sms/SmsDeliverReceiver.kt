package com.example.sms

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.data.local.SalimDatabase
import com.example.notifications.MessagingNotificationManager
import com.example.util.PhoneNumberHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsDeliverReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SalimDatabase.getDatabase(context)
                val fullBody = StringBuilder()
                var sender = ""
                var timestamp = System.currentTimeMillis()

                for (msg in messages) {
                    if (sender.isBlank()) {
                        sender = msg.displayOriginatingAddress ?: msg.originatingAddress ?: ""
                    }
                    fullBody.append(msg.displayMessageBody ?: msg.messageBody ?: "")
                    timestamp = msg.timestampMillis
                }

                val normalizedSender = PhoneNumberHelper.normalize(sender)

                // Check if blocked in Salim
                if (db.salimDao().isNumberBlocked(normalizedSender)) {
                    // Blocked number: do not notify or surface to inbox
                    return@launch
                }

                // Write into SMS Inbox Provider (as default SMS app)
                val values = ContentValues().apply {
                    put(Telephony.Sms.ADDRESS, sender)
                    put(Telephony.Sms.BODY, fullBody.toString())
                    put(Telephony.Sms.DATE, timestamp)
                    put(Telephony.Sms.READ, 0)
                    put(Telephony.Sms.SEEN, 0)
                    put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                }

                val uri = context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
                val threadId = uri?.lastPathSegment?.toLongOrNull() ?: 0L

                // Show real notification
                MessagingNotificationManager.showIncomingMessageNotification(
                    context = context,
                    sender = sender,
                    body = fullBody.toString(),
                    threadId = threadId
                )
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
