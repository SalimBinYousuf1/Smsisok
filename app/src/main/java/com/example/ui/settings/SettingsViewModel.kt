package com.example.ui.settings

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.BlockedNumberEntity
import com.example.data.model.ConversationItem
import com.example.data.model.SimInfo
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SalimSettings
import com.example.data.repository.ThemeMode
import com.example.util.PhoneNumberHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiagnosticsInfo(
    val appVersion: String = "1.0.0",
    val androidVersion: String = Build.VERSION.RELEASE,
    val sdkInt: Int = Build.VERSION.SDK_INT,
    val isDefaultSms: Boolean = false,
    val hasSmsPermission: Boolean = false,
    val hasContactsPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val simCount: Int = 0,
    val sims: List<SimInfo> = emptyList(),
    val totalConversations: Int = 0,
    val totalBlocked: Int = 0
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val preferencesRepository = PreferencesRepository(application)
    private val dao = app.database.salimDao()
    private val smsRepo = app.smsRepository
    private val roleManager = app.roleAndPermissionManager

    val settings: StateFlow<SalimSettings> = preferencesRepository.settings

    val blockedNumbers: StateFlow<List<BlockedNumberEntity>> = dao.getAllBlocked()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Master stream of archived conversations
    val archivedConversations: StateFlow<List<ConversationItem>> = combine(
        smsRepo.conversations,
        dao.getAllArchived()
    ) { baseList, archivedList ->
        val archivedIds = archivedList.map { it.threadId }.toSet()
        baseList.filter { archivedIds.contains(it.threadId) }
            .map { it.copy(isArchived = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _diagnostics = MutableStateFlow(DiagnosticsInfo())
    val diagnostics: StateFlow<DiagnosticsInfo> = _diagnostics.asStateFlow()

    init {
        refreshDiagnostics()
    }

    fun refreshDiagnostics() {
        viewModelScope.launch {
            val sims = app.subscriptionRepository.activeSims.value
            val totalConvs = smsRepo.conversations.value.size
            val blocked = dao.getAllBlocked()
            _diagnostics.value = DiagnosticsInfo(
                appVersion = "1.0.0 (Build 100)",
                androidVersion = Build.VERSION.RELEASE,
                sdkInt = Build.VERSION.SDK_INT,
                isDefaultSms = roleManager.isDefaultSmsApp(),
                hasSmsPermission = roleManager.hasSmsPermissions(),
                hasContactsPermission = roleManager.hasContactsPermission(),
                hasNotificationPermission = roleManager.hasNotificationPermission(),
                simCount = sims.size,
                sims = sims,
                totalConversations = totalConvs,
                totalBlocked = blockedNumbers.value.size
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        preferencesRepository.setThemeMode(mode)
    }

    fun setEnterKeySends(enabled: Boolean) {
        preferencesRepository.setEnterKeySends(enabled)
    }

    fun setDeliveryReports(enabled: Boolean) {
        preferencesRepository.setDeliveryReports(enabled)
    }

    fun setSoundFeedback(enabled: Boolean) {
        preferencesRepository.setSoundFeedback(enabled)
    }

    fun setHapticFeedback(enabled: Boolean) {
        preferencesRepository.setHapticFeedback(enabled)
    }

    fun setConfirmDelete(enabled: Boolean) {
        preferencesRepository.setConfirmDelete(enabled)
    }

    fun blockNumber(number: String, note: String = "") {
        if (number.isBlank()) return
        viewModelScope.launch {
            val normalized = PhoneNumberHelper.normalize(number)
            dao.blockNumber(
                BlockedNumberEntity(
                    normalizedNumber = normalized,
                    rawNumber = number.trim(),
                    note = note
                )
            )
        }
    }

    fun unblockNumber(normalizedNumber: String) {
        viewModelScope.launch {
            dao.unblockNumber(normalizedNumber)
        }
    }

    fun unarchiveConversation(threadId: Long) {
        viewModelScope.launch {
            dao.unarchiveConversation(threadId)
        }
    }

    fun clearLocalMetadata() {
        viewModelScope.launch {
            dao.deleteAllDrafts()
            dao.deleteAllSearches()
        }
    }
}
