package com.example.ui.compose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.model.ContactItem
import com.example.data.model.SimInfo
import com.example.sms.SendResult
import com.example.util.PhoneNumberHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ComposeViewModel(
    application: Application,
    initialAddress: String = "",
    initialBody: String = ""
) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val contactsRepo = app.contactsRepository
    private val smsSender = app.smsSender

    private val _recipientInput = MutableStateFlow(initialAddress)
    val recipientInput: StateFlow<String> = _recipientInput.asStateFlow()

    private val _selectedContact = MutableStateFlow<ContactItem?>(null)
    val selectedContact: StateFlow<ContactItem?> = _selectedContact.asStateFlow()

    private val _messageBody = MutableStateFlow(initialBody)
    val messageBody: StateFlow<String> = _messageBody.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val activeSims: StateFlow<List<SimInfo>> = app.subscriptionRepository.activeSims

    private val _selectedSimId = MutableStateFlow<Int?>(null)
    val selectedSimId: StateFlow<Int?> = _selectedSimId.asStateFlow()

    val contactSuggestions: StateFlow<List<ContactItem>> = combine(
        contactsRepo.contacts,
        _recipientInput,
        _selectedContact
    ) { allContacts, query, selected ->
        if (selected != null || query.isBlank()) {
            emptyList()
        } else {
            contactsRepo.searchContacts(query, isT9 = false).take(8)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (initialAddress.isNotBlank()) {
            val contact = contactsRepo.findContactByAddress(initialAddress)
            if (contact != null) {
                _selectedContact.value = contact
            }
        }
    }

    fun setRecipientInput(input: String) {
        _recipientInput.value = input
        _selectedContact.value = null
    }

    fun selectContact(contact: ContactItem) {
        _selectedContact.value = contact
        _recipientInput.value = contact.phoneNumber
    }

    fun setMessageBody(body: String) {
        _messageBody.value = body
    }

    fun setSelectedSim(subId: Int?) {
        _selectedSimId.value = subId
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun sendMessage(onSuccess: (destinationAddress: String) -> Unit) {
        val address = (_selectedContact.value?.phoneNumber ?: _recipientInput.value).trim()
        val body = _messageBody.value.trim()

        if (address.isBlank()) {
            _errorMessage.value = "Please enter a valid recipient number or contact."
            return
        }

        if (!PhoneNumberHelper.isValidRecipient(address)) {
            _errorMessage.value = "Invalid recipient address: $address"
            return
        }

        if (body.isBlank()) {
            _errorMessage.value = "Message cannot be empty."
            return
        }

        _isSending.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = smsSender.sendSms(
                destinationAddress = address,
                messageText = body,
                subscriptionId = _selectedSimId.value
            )

            _isSending.value = false
            when (result) {
                is SendResult.Success -> {
                    onSuccess(address)
                }
                is SendResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
        }
    }
}
