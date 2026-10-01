package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_conversations")
data class PinnedConversationEntity(
    @PrimaryKey val threadId: Long,
    val pinnedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "archived_conversations")
data class ArchivedConversationEntity(
    @PrimaryKey val threadId: Long,
    val archivedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "muted_conversations")
data class MutedConversationEntity(
    @PrimaryKey val threadId: Long,
    val mutedUntil: Long = Long.MAX_VALUE
)

@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey val threadId: Long,
    val address: String,
    val text: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val normalizedNumber: String,
    val rawNumber: String,
    val blockedAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
