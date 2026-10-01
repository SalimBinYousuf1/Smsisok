package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SalimDao {

    // Pinned
    @Query("SELECT * FROM pinned_conversations ORDER BY pinnedAt DESC")
    fun getAllPinned(): Flow<List<PinnedConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinConversation(entity: PinnedConversationEntity)

    @Query("DELETE FROM pinned_conversations WHERE threadId = :threadId")
    suspend fun unpinConversation(threadId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM pinned_conversations WHERE threadId = :threadId)")
    suspend fun isPinned(threadId: Long): Boolean

    // Archived
    @Query("SELECT * FROM archived_conversations ORDER BY archivedAt DESC")
    fun getAllArchived(): Flow<List<ArchivedConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun archiveConversation(entity: ArchivedConversationEntity)

    @Query("DELETE FROM archived_conversations WHERE threadId = :threadId")
    suspend fun unarchiveConversation(threadId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM archived_conversations WHERE threadId = :threadId)")
    suspend fun isArchived(threadId: Long): Boolean

    // Muted
    @Query("SELECT * FROM muted_conversations")
    fun getAllMuted(): Flow<List<MutedConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun muteConversation(entity: MutedConversationEntity)

    @Query("DELETE FROM muted_conversations WHERE threadId = :threadId")
    suspend fun unmuteConversation(threadId: Long)

    // Drafts
    @Query("SELECT * FROM drafts WHERE threadId = :threadId LIMIT 1")
    suspend fun getDraft(threadId: Long): DraftEntity?

    @Query("SELECT * FROM drafts")
    fun getAllDrafts(): Flow<List<DraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: DraftEntity)

    @Query("DELETE FROM drafts WHERE threadId = :threadId")
    suspend fun deleteDraft(threadId: Long)

    // Blocked numbers
    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun getAllBlocked(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE normalizedNumber = :normalizedNumber)")
    suspend fun isNumberBlocked(normalizedNumber: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockNumber(entity: BlockedNumberEntity)

    @Query("DELETE FROM blocked_numbers WHERE normalizedNumber = :normalizedNumber")
    suspend fun unblockNumber(normalizedNumber: String)

    @Query("DELETE FROM blocked_numbers WHERE id = :id")
    suspend fun unblockById(id: Long)

    // Search History
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    // Clear all Salim-owned local metadata
    @Query("DELETE FROM search_history")
    suspend fun deleteAllSearches()

    @Query("DELETE FROM drafts")
    suspend fun deleteAllDrafts()
}
