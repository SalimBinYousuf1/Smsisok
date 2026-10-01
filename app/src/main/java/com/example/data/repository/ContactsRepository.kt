package com.example.data.repository

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.model.ContactItem
import com.example.util.PhoneNumberHelper
import com.example.util.T9Matcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _contacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val contacts: StateFlow<List<ContactItem>> = _contacts.asStateFlow()

    // Cache of normalized number to ContactItem for instant lookup in message lists
    private var contactMapByNumber = HashMap<String, ContactItem>()

    private val contactsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            refreshContacts()
        }
    }

    init {
        try {
            contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                true,
                contactsObserver
            )
        } catch (_: Exception) {}
        refreshContacts()
    }

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun refreshContacts() {
        if (!hasContactsPermission()) {
            _contacts.value = emptyList()
            contactMapByNumber.clear()
            return
        }

        scope.launch {
            val list = queryContacts()
            _contacts.value = list

            val map = HashMap<String, ContactItem>()
            for (contact in list) {
                val norm = contact.normalizedNumber.ifBlank { PhoneNumberHelper.normalize(contact.phoneNumber) }
                if (norm.isNotBlank()) {
                    map[norm] = contact
                    // Also store last 7-10 digits for matching local vs international
                    if (norm.length > 7) {
                        map[norm.takeLast(7)] = contact
                    }
                    if (norm.length > 10) {
                        map[norm.takeLast(10)] = contact
                    }
                }
            }
            contactMapByNumber = map
        }
    }

    private suspend fun queryContacts(): List<ContactItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ContactItem>()
        if (!hasContactsPermission()) return@withContext results

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL
        )

        try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val normNumIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
                val photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                val starredIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)
                val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                val labelIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)

                val seen = HashSet<String>()
                while (cursor.moveToNext()) {
                    val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "Unknown" else "Unknown"
                    val number = if (numIdx >= 0) cursor.getString(numIdx) ?: "" else ""
                    val normNumber = if (normNumIdx >= 0) cursor.getString(normNumIdx) ?: "" else ""
                    val photoUri = if (photoIdx >= 0) cursor.getString(photoIdx) else null
                    val starred = if (starredIdx >= 0) cursor.getInt(starredIdx) == 1 else false
                    val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else 0
                    val customLabel = if (labelIdx >= 0) cursor.getString(labelIdx) else null

                    val label = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                        context.resources,
                        type,
                        customLabel
                    ).toString()

                    val dedupKey = "$name:$number"
                    if (seen.add(dedupKey)) {
                        results.add(
                            ContactItem(
                                id = id,
                                displayName = name,
                                phoneNumber = number,
                                normalizedNumber = normNumber.ifBlank { PhoneNumberHelper.normalize(number) },
                                photoUri = photoUri,
                                starred = starred,
                                label = label
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        results
    }

    fun findContactByAddress(address: String): ContactItem? {
        if (address.isBlank()) return null
        val norm = PhoneNumberHelper.normalize(address)
        if (norm.isBlank()) return null

        contactMapByNumber[norm]?.let { return it }
        if (norm.length > 7) {
            contactMapByNumber[norm.takeLast(7)]?.let { return it }
        }
        if (norm.length > 10) {
            contactMapByNumber[norm.takeLast(10)]?.let { return it }
        }

        // Direct search across current list
        return _contacts.value.firstOrNull {
            PhoneNumberHelper.areEqual(it.phoneNumber, address)
        }
    }

    fun searchContacts(query: String, isT9: Boolean = false): List<ContactItem> {
        val current = _contacts.value
        if (query.isBlank()) return current

        return if (isT9) {
            current.filter { contact ->
                T9Matcher.matches(contact.displayName, contact.phoneNumber, query)
            }
        } else {
            val q = query.trim().lowercase()
            current.filter { contact ->
                contact.displayName.lowercase().contains(q) ||
                    contact.phoneNumber.contains(q) ||
                    contact.normalizedNumber.contains(q)
            }
        }
    }
}
