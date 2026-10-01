package com.example.ui.conversations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.ArchivedConversationEntity
import com.example.data.local.PinnedConversationEntity
import com.example.data.model.ConversationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ConversationFilter {
    ALL,
    UNREAD,
    PINNED
}

class ConversationsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val dao = app.database.salimDao()
    private val smsRepo = app.smsRepository

    val isLoading: StateFlow<Boolean> = smsRepo.isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ConversationFilter.ALL)
    val selectedFilter: StateFlow<ConversationFilter> = _selectedFilter.asStateFlow()

    private val _selectedThreadIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedThreadIds: StateFlow<Set<Long>> = _selectedThreadIds.asStateFlow()

    val isSelectionMode: StateFlow<Boolean> = _selectedThreadIds.combine(_selectedThreadIds) { ids, _ ->
        ids.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Flow 1: Combine data sources
    private val activeConversations = combine(
        smsRepo.conversations,
        dao.getAllPinned(),
        dao.getAllArchived(),
        dao.getAllDrafts()
    ) { baseList, pinnedList, archivedList, draftsList ->
        val pinnedIds = pinnedList.map { it.threadId }.toSet()
        val archivedIds = archivedList.map { it.threadId }.toSet()
        val draftsMap = draftsList.associate { it.threadId to it.text }

        baseList.filter { !archivedIds.contains(it.threadId) }.map { item ->
            item.copy(
                isPinned = pinnedIds.contains(item.threadId),
                draftSnippet = draftsMap[item.threadId]
            )
        }
    }

    // Master stream of filtered conversations
    val conversations: StateFlow<List<ConversationItem>> = combine(
        activeConversations,
        _searchQuery,
        _selectedFilter
    ) { active, query, filter ->
        val searchFiltered = if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            active.filter { item ->
                item.displayTitle.lowercase().contains(q) ||
                    item.address.contains(q) ||
                    item.snippet.lowercase().contains(q)
            }
        } else {
            active
        }

        val tabFiltered = when (filter) {
            ConversationFilter.ALL -> searchFiltered
            ConversationFilter.UNREAD -> searchFiltered.filter { it.unreadCount > 0 }
            ConversationFilter.PINNED -> searchFiltered.filter { it.isPinned }
        }

        tabFiltered.sortedWith(
            compareByDescending<ConversationItem> { it.isPinned }
                .thenByDescending { it.date }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedConversations: StateFlow<List<ConversationItem>> = activeConversations.combine(_selectedFilter) { list, _ ->
        list.filter { it.isPinned }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: ConversationFilter) {
        _selectedFilter.value = filter
    }

    fun toggleSelection(threadId: Long) {
        val current = _selectedThreadIds.value.toMutableSet()
        if (current.contains(threadId)) {
            current.remove(threadId)
        } else {
            current.add(threadId)
        }
        _selectedThreadIds.value = current
    }

    fun selectAll() {
        _selectedThreadIds.value = conversations.value.map { it.threadId }.toSet()
    }

    fun clearSelection() {
        _selectedThreadIds.value = emptySet()
    }

    fun togglePin(threadId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            if (isPinned) {
                dao.unpinConversation(threadId)
            } else {
                dao.pinConversation(PinnedConversationEntity(threadId))
            }
        }
    }

    fun archiveConversation(threadId: Long) {
        viewModelScope.launch {
            dao.archiveConversation(ArchivedConversationEntity(threadId))
            dao.unpinConversation(threadId)
        }
    }

    fun markAsRead(threadId: Long) {
        viewModelScope.launch {
            smsRepo.markThreadAsRead(threadId)
        }
    }

    fun markAsUnread(threadId: Long) {
        viewModelScope.launch {
            smsRepo.markThreadAsUnread(threadId)
        }
    }

    fun deleteConversation(threadId: Long) {
        viewModelScope.launch {
            smsRepo.deleteConversation(threadId)
            dao.unpinConversation(threadId)
            dao.unarchiveConversation(threadId)
            dao.deleteDraft(threadId)
        }
    }

    fun batchDeleteSelected() {
        val targets = _selectedThreadIds.value.toList()
        viewModelScope.launch {
            for (id in targets) {
                smsRepo.deleteConversation(id)
                dao.unpinConversation(id)
                dao.unarchiveConversation(id)
                dao.deleteDraft(id)
            }
            clearSelection()
        }
    }

    fun batchArchiveSelected() {
        val targets = _selectedThreadIds.value.toList()
        viewModelScope.launch {
            for (id in targets) {
                dao.archiveConversation(ArchivedConversationEntity(id))
                dao.unpinConversation(id)
            }
            clearSelection()
        }
    }

    fun batchMarkReadSelected() {
        val targets = _selectedThreadIds.value.toList()
        viewModelScope.launch {
            for (id in targets) {
                smsRepo.markThreadAsRead(id)
            }
            clearSelection()
        }
    }

    fun batchMarkUnreadSelected() {
        val targets = _selectedThreadIds.value.toList()
        viewModelScope.launch {
            for (id in targets) {
                smsRepo.markThreadAsUnread(id)
            }
            clearSelection()
        }
    }
}
