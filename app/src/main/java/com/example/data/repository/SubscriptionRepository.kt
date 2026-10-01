package com.example.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.example.data.model.SimInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubscriptionRepository(private val context: Context) {

    private val _activeSims = MutableStateFlow<List<SimInfo>>(emptyList())
    val activeSims: StateFlow<List<SimInfo>> = _activeSims.asStateFlow()

    private val _defaultSmsSubId = MutableStateFlow(SubscriptionManager.getDefaultSmsSubscriptionId())
    val defaultSmsSubId: StateFlow<Int> = _defaultSmsSubId.asStateFlow()

    init {
        refreshSubscriptions()
    }

    fun hasPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun refreshSubscriptions() {
        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            if (subscriptionManager == null) {
                _activeSims.value = emptyList()
                return
            }

            val defaultSubId = SubscriptionManager.getDefaultSmsSubscriptionId()
            _defaultSmsSubId.value = defaultSubId

            if (hasPhoneStatePermission() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val list = subscriptionManager.activeSubscriptionInfoList ?: emptyList()
                _activeSims.value = list.map { info: SubscriptionInfo ->
                    SimInfo(
                        subscriptionId = info.subscriptionId,
                        displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                        carrierName = info.carrierName?.toString() ?: "",
                        simSlotIndex = info.simSlotIndex,
                        isDefaultSms = info.subscriptionId == defaultSubId
                    )
                }
            } else {
                _activeSims.value = emptyList()
            }
        } catch (_: SecurityException) {
            _activeSims.value = emptyList()
        } catch (_: Exception) {
            _activeSims.value = emptyList()
        }
    }
}
