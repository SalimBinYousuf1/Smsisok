package com.example.ui.conversation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.DraftEntity
import com.example.data.model.ContactItem
import com.example.data.model.SimInfo
import com.example.data.model.SmsMessageItem
import com.example.sms.SendResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationDetailViewModel(
    application: Application,
    private val threadId: Long,
    val address: String
) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val smsRepo = app.smsRepository
    private val dao = app.database.salimDao()

    private val _messages = MutableStateFlow<List<SmsMessageItem>>(emptyList())
    val messages: StateFlow<List<SmsMessageItem>> = _messages.asStateFlow()

    private val _draftText = MutableStateFlow("")
    val draftText: StateFlow<String> = _draftText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _sendError = MutableStateFlow<String?>(null)
    val sendError: StateFlow<String?> = _sendError.asStateFlow()

    val contact: StateFlow<ContactItem?> = MutableStateFlow(
        app.contactsRepository.findContactByAddress(address)
    ).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val activeSims: StateFlow<List<SimInfo>> = app.subscriptionRepository.activeSims

    private val _selectedSimId = MutableStateFlow<Int?>(null)
    val selectedSimId: StateFlow<Int?> = _selectedSimId.asStateFlow()

    init {
        loadMessages()
        loadDraft()

        viewModelScope.launch {
            smsRepo.messagesChangedEvent.collect {
                loadMessages()
            }
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            val list = smsRepo.getMessagesForThread(threadId)
            _messages.value = list
            smsRepo.markThreadAsRead(threadId)
        }
    }

    private fun loadDraft() {
        viewModelScope.launch {
            val savedDraft = dao.getDraft(threadId)
            if (savedDraft != null && savedDraft.text.isNotBlank()) {
                _draftText.value = savedDraft.text
            }
        }
    }

    fun updateDraft(text: String) {
        _draftText.value = text
        viewModelScope.launch {
            if (text.isBlank()) {
                dao.deleteDraft(threadId)
            } else {
                dao.saveDraft(DraftEntity(threadId, address, text))
            }
        }
    }

    fun setSelectedSim(subId: Int?) {
        _selectedSimId.value = subId
    }

    fun clearSendError() {
        _sendError.value = null
    }

    fun sendMessage() {
        val text = _draftText.value.trim()
        if (text.isBlank() || _isSending.value) return

        _isSending.value = true
        _sendError.value = null

        viewModelScope.launch {
            val result = app.smsSender.sendSms(
                destinationAddress = address,
                messageText = text,
                subscriptionId = _selectedSimId.value
            )

            _isSending.value = false
            when (result) {
                is SendResult.Success -> {
                    _draftText.value = ""
                    dao.deleteDraft(threadId)
                    loadMessages()
                }
                is SendResult.Error -> {
                    _sendError.value = result.message
                }
            }
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            smsRepo.deleteMessage(messageId)
            loadMessages()
        }
    }

    fun deleteConversation(onDeleted: () -> Unit) {
        viewModelScope.launch {
            smsRepo.deleteConversation(threadId)
            dao.deleteDraft(threadId)
            dao.unpinConversation(threadId)
            dao.unarchiveConversation(threadId)
            onDeleted()
        }
    }
}
