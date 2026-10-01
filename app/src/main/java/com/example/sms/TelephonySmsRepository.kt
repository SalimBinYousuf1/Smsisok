package com.example.sms

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.example.data.model.ConversationItem
import com.example.data.model.SmsMessageItem
import com.example.data.repository.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelephonySmsRepository(
    private val context: Context,
    private val contactsRepository: ContactsRepository
) {
    private val contentResolver: ContentResolver = context.contentResolver
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _conversations = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversations: StateFlow<List<ConversationItem>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Shared flow to notify detail screens when SMS table changes
    private val _messagesChangedEvent = MutableSharedFlow<Long>(extraBufferCapacity = 10)
    val messagesChangedEvent: SharedFlow<Long> = _messagesChangedEvent.asSharedFlow()

    private val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            refreshConversations()
            _messagesChangedEvent.tryEmit(System.currentTimeMillis())
        }
    }

    init {
        try {
            contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                smsObserver
            )
        } catch (_: Exception) {}
        refreshConversations()
    }

    fun hasReadSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun refreshConversations() {
        if (!hasReadSmsPermission()) {
            _conversations.value = emptyList()
            return
        }

        scope.launch {
            _isLoading.value = true
            val list = queryConversationsInternal()
            _conversations.value = list
            _isLoading.value = false
        }
    }

    private suspend fun queryConversationsInternal(): List<ConversationItem> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<ConversationItem>()
        if (!hasReadSmsPermission()) return@withContext resultList

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.STATUS
        )

        try {
            contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
                val threadIdIdx = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                val readIdx = cursor.getColumnIndex(Telephony.Sms.READ)
                val typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE)
                val statusIdx = cursor.getColumnIndex(Telephony.Sms.STATUS)

                data class AggregatedThread(
                    var latestMessageId: Long = 0,
                    var address: String = "",
                    var snippet: String = "",
                    var date: Long = 0,
                    var messageCount: Int = 0,
                    var unreadCount: Int = 0,
                    var hasFailed: Boolean = false
                )

                val threadMap = LinkedHashMap<Long, AggregatedThread>()

                while (cursor.moveToNext()) {
                    val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                    var threadId = if (threadIdIdx >= 0) cursor.getLong(threadIdIdx) else 0L
                    val address = if (addressIdx >= 0) cursor.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) cursor.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L
                    val read = if (readIdx >= 0) cursor.getInt(readIdx) == 1 else true
                    val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else 1
                    val status = if (statusIdx >= 0) cursor.getInt(statusIdx) else -1

                    if (threadId <= 0L) {
                        // Fallback threadId by address hash code
                        threadId = address.hashCode().toLong()
                    }

                    var agg = threadMap[threadId]
                    if (agg == null) {
                        agg = AggregatedThread(
                            latestMessageId = id,
                            address = address,
                            snippet = body,
                            date = date,
                            messageCount = 1,
                            unreadCount = if (!read) 1 else 0,
                            hasFailed = (type == Telephony.Sms.MESSAGE_TYPE_FAILED || status == Telephony.Sms.STATUS_FAILED)
                        )
                        threadMap[threadId] = agg
                    } else {
                        agg.messageCount += 1
                        if (!read) agg.unreadCount += 1
                        if (type == Telephony.Sms.MESSAGE_TYPE_FAILED || status == Telephony.Sms.STATUS_FAILED) {
                            agg.hasFailed = true
                        }
                    }
                }

                for ((threadId, agg) in threadMap) {
                    val contact = contactsRepository.findContactByAddress(agg.address)
                    resultList.add(
                        ConversationItem(
                            threadId = threadId,
                            address = agg.address,
                            contactName = contact?.displayName,
                            contactPhotoUri = contact?.photoUri,
                            snippet = agg.snippet,
                            date = agg.date,
                            messageCount = agg.messageCount,
                            unreadCount = agg.unreadCount,
                            hasFailedMessage = agg.hasFailed
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        resultList
    }

    suspend fun getMessagesForThread(threadId: Long): List<SmsMessageItem> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<SmsMessageItem>()
        if (!hasReadSmsPermission()) return@withContext messages

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.STATUS,
            Telephony.Sms.SUBSCRIPTION_ID
        )

        try {
            contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString()),
                "${Telephony.Sms.DATE} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
                val threadIdIdx = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                val readIdx = cursor.getColumnIndex(Telephony.Sms.READ)
                val typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE)
                val statusIdx = cursor.getColumnIndex(Telephony.Sms.STATUS)
                val subIdIdx = cursor.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)

                while (cursor.moveToNext()) {
                    val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                    val tId = if (threadIdIdx >= 0) cursor.getLong(threadIdIdx) else threadId
                    val address = if (addressIdx >= 0) cursor.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) cursor.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L
                    val read = if (readIdx >= 0) cursor.getInt(readIdx) == 1 else true
                    val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else 1
                    val status = if (statusIdx >= 0) cursor.getInt(statusIdx) else -1
                    val subId = if (subIdIdx >= 0) cursor.getInt(subIdIdx) else -1

                    messages.add(
                        SmsMessageItem(
                            id = id,
                            threadId = tId,
                            address = address,
                            body = body,
                            date = date,
                            type = type,
                            read = read,
                            status = status,
                            subId = subId
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        messages
    }

    suspend fun markThreadAsRead(threadId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.SEEN, 1)
            }
            val count = contentResolver.update(
                Telephony.Sms.Inbox.CONTENT_URI,
                values,
                "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.READ} = 0",
                arrayOf(threadId.toString())
            )
            refreshConversations()
            count >= 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun markThreadAsUnread(threadId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 0)
            }
            val count = contentResolver.update(
                Telephony.Sms.Inbox.CONTENT_URI,
                values,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString())
            )
            refreshConversations()
            count >= 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteMessage(messageId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val count = contentResolver.delete(
                Telephony.Sms.CONTENT_URI,
                "${Telephony.Sms._ID} = ?",
                arrayOf(messageId.toString())
            )
            refreshConversations()
            _messagesChangedEvent.tryEmit(System.currentTimeMillis())
            count > 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteConversation(threadId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val count = contentResolver.delete(
                Telephony.Sms.CONTENT_URI,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString())
            )
            refreshConversations()
            _messagesChangedEvent.tryEmit(System.currentTimeMillis())
            count > 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun searchMessages(query: String): List<SmsMessageItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SmsMessageItem>()
        if (query.isBlank() || !hasReadSmsPermission()) return@withContext results

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.STATUS
        )

        try {
            contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                "${Telephony.Sms.BODY} LIKE ? OR ${Telephony.Sms.ADDRESS} LIKE ?",
                arrayOf("%$query%", "%$query%"),
                "${Telephony.Sms.DATE} DESC LIMIT 50"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
                val threadIdIdx = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                val readIdx = cursor.getColumnIndex(Telephony.Sms.READ)
                val typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE)
                val statusIdx = cursor.getColumnIndex(Telephony.Sms.STATUS)

                while (cursor.moveToNext()) {
                    val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                    val tId = if (threadIdIdx >= 0) cursor.getLong(threadIdIdx) else 0L
                    val address = if (addressIdx >= 0) cursor.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) cursor.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L
                    val read = if (readIdx >= 0) cursor.getInt(readIdx) == 1 else true
                    val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else 1
                    val status = if (statusIdx >= 0) cursor.getInt(statusIdx) else -1

                    results.add(
                        SmsMessageItem(
                            id = id,
                            threadId = tId,
                            address = address,
                            body = body,
                            date = date,
                            type = type,
                            read = read,
                            status = status
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        results
    }
}
