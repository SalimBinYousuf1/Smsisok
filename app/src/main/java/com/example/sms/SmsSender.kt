package com.example.sms

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.util.PhoneNumberHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class SendResult {
    data class Success(val messageUri: String? = null) : SendResult()
    data class Error(val message: String) : SendResult()
}

class SmsSender(private val context: Context) {

    fun hasSendSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    suspend fun sendSms(
        destinationAddress: String,
        messageText: String,
        subscriptionId: Int? = null
    ): SendResult = withContext(Dispatchers.IO) {
        if (!hasSendSmsPermission()) {
            return@withContext SendResult.Error("SEND_SMS permission is not granted.")
        }

        if (destinationAddress.isBlank()) {
            return@withContext SendResult.Error("Recipient phone number is empty.")
        }

        if (messageText.isBlank()) {
            return@withContext SendResult.Error("Message text is empty.")
        }

        val cleanNumber = destinationAddress.trim()

        try {
            val smsManager: SmsManager = if (subscriptionId != null && subscriptionId >= 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            }

            val parts = smsManager.divideMessage(messageText)

            val sentIntent = PendingIntent.getBroadcast(
                context,
                System.currentTimeMillis().toInt(),
                Intent("com.example.salim.SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val deliveryIntent = PendingIntent.getBroadcast(
                context,
                System.currentTimeMillis().toInt() + 1,
                Intent("com.example.salim.SMS_DELIVERED"),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent>()
                val deliveryIntents = ArrayList<PendingIntent>()
                for (i in parts.indices) {
                    sentIntents.add(sentIntent)
                    deliveryIntents.add(deliveryIntent)
                }
                smsManager.sendMultipartTextMessage(
                    cleanNumber,
                    null,
                    parts,
                    sentIntents,
                    deliveryIntents
                )
            } else {
                smsManager.sendTextMessage(
                    cleanNumber,
                    null,
                    messageText,
                    sentIntent,
                    deliveryIntent
                )
            }

            // Write to Sent SMS provider if permitted / default SMS app
            var insertedUri: String? = null
            try {
                val values = ContentValues().apply {
                    put(Telephony.Sms.ADDRESS, cleanNumber)
                    put(Telephony.Sms.BODY, messageText)
                    put(Telephony.Sms.DATE, System.currentTimeMillis())
                    put(Telephony.Sms.READ, 1)
                    put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                    put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE)
                    if (subscriptionId != null && subscriptionId >= 0) {
                        put(Telephony.Sms.SUBSCRIPTION_ID, subscriptionId)
                    }
                }
                val uri = context.contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
                insertedUri = uri?.toString()
            } catch (_: Exception) {
                // If not default SMS app, system writes or prevents write
            }

            SendResult.Success(insertedUri)
        } catch (se: SecurityException) {
            SendResult.Error("SMS permission denied: ${se.localizedMessage}")
        } catch (e: Exception) {
            SendResult.Error("Failed to send SMS: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
