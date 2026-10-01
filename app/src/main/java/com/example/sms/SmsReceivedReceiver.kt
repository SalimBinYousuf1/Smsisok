package com.example.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.data.local.SalimDatabase
import com.example.notifications.MessagingNotificationManager
import com.example.util.PhoneNumberHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceivedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val roleManager = RoleAndPermissionManager(context)
        // If Salim is the default SMS app, SmsDeliverReceiver already handled it
        if (roleManager.isDefaultSmsApp()) {
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SalimDatabase.getDatabase(context)
                val fullBody = StringBuilder()
                var sender = ""

                for (msg in messages) {
                    if (sender.isBlank()) {
                        sender = msg.displayOriginatingAddress ?: msg.originatingAddress ?: ""
                    }
                    fullBody.append(msg.displayMessageBody ?: msg.messageBody ?: "")
                }

                val normalizedSender = PhoneNumberHelper.normalize(sender)
                if (db.salimDao().isNumberBlocked(normalizedSender)) {
                    return@launch
                }

                // Show notification when not default SMS app
                MessagingNotificationManager.showIncomingMessageNotification(
                    context = context,
                    sender = sender,
                    body = fullBody.toString()
                )
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
