package com.example.data.model

data class SmsMessageItem(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: Int, // 1 = Inbox, 2 = Sent, 3 = Draft, 4 = Outbox, 5 = Failed, 6 = Queued
    val read: Boolean,
    val status: Int = -1, // -1 = None, 0 = Complete, 32 = Pending, 64 = Failed
    val subId: Int = -1,
    val isMms: Boolean = false,
    val attachmentUri: String? = null
) {
    val isOutgoing: Boolean
        get() = type == 2 || type == 4 || type == 5 || type == 6

    val isFailed: Boolean
        get() = type == 5 || status == 64

    val isPending: Boolean
        get() = type == 4 || type == 6 || status == 32
}

data class ConversationItem(
    val threadId: Long,
    val address: String,
    val contactName: String? = null,
    val contactPhotoUri: String? = null,
    val snippet: String = "",
    val date: Long = 0L,
    val messageCount: Int = 0,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val hasFailedMessage: Boolean = false,
    val draftSnippet: String? = null
) {
    val displayTitle: String
        get() = if (!contactName.isNullOrBlank()) contactName else address.ifBlank { "Unknown" }
}

data class ContactItem(
    val id: Long,
    val displayName: String,
    val phoneNumber: String,
    val normalizedNumber: String,
    val photoUri: String? = null,
    val starred: Boolean = false,
    val label: String? = null
)

data class SimInfo(
    val subscriptionId: Int,
    val displayName: String,
    val carrierName: String,
    val simSlotIndex: Int,
    val isDefaultSms: Boolean
)
