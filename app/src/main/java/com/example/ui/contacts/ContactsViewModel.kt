package com.example.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.model.ContactItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ContactsTab {
    ALL,
    FAVORITES
}

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val contactsRepo = app.contactsRepository

    private val _selectedTab = MutableStateFlow(ContactsTab.ALL)
    val selectedTab: StateFlow<ContactsTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isT9Mode = MutableStateFlow(false)
    val isT9Mode: StateFlow<Boolean> = _isT9Mode.asStateFlow()

    val filteredContacts: StateFlow<List<ContactItem>> = combine(
        contactsRepo.contacts,
        _searchQuery,
        _isT9Mode,
        _selectedTab
    ) { allContacts, query, isT9, tab ->
        val tabFiltered = if (tab == ContactsTab.FAVORITES) {
            allContacts.filter { it.starred }
        } else {
            allContacts
        }

        if (query.isBlank()) {
            tabFiltered
        } else {
            contactsRepo.searchContacts(query, isT9 = isT9).filter {
                if (tab == ContactsTab.FAVORITES) it.starred else true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedContacts: StateFlow<Map<Char, List<ContactItem>>> = filteredContacts.combine(_isT9Mode) { list, _ ->
        list.groupBy {
            val first = it.displayName.firstOrNull()?.uppercaseChar() ?: '#'
            if (first in 'A'..'Z') first else '#'
        }.toSortedMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setSelectedTab(tab: ContactsTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleT9Mode() {
        _isT9Mode.value = !_isT9Mode.value
        _searchQuery.value = ""
    }

    fun appendT9Digit(digit: Char) {
        _searchQuery.value = _searchQuery.value + digit
    }

    fun backspaceT9() {
        val current = _searchQuery.value
        if (current.isNotEmpty()) {
            _searchQuery.value = current.dropLast(1)
        }
    }

    fun clearT9() {
        _searchQuery.value = ""
    }

    fun refresh() {
        contactsRepo.refreshContacts()
    }
}
