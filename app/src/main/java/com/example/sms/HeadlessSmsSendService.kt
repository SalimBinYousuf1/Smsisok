package com.example.sms

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.telephony.TelephonyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeadlessSmsSendService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val action = intent.action
        if (TelephonyManager.ACTION_RESPOND_VIA_MESSAGE == action) {
            val extras = intent.extras ?: return START_NOT_STICKY
            val messageText = extras.getString(Intent.EXTRA_TEXT)
            val recipientUri: Uri? = intent.data
            val recipient = recipientUri?.schemeSpecificPart

            if (!messageText.isNullOrBlank() && !recipient.isNullOrBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val sender = SmsSender(applicationContext)
                        sender.sendSms(recipient, messageText)
                    } finally {
                        stopSelf()
                    }
                }
                return START_STICKY
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }
}
