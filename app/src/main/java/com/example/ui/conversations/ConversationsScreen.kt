package com.example.ui.conversations

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationItem
import com.example.ui.components.AvatarView
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimDestructive
import com.example.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConversationsScreen(
    conversations: List<ConversationItem>,
    searchQuery: String,
    selectedFilter: ConversationFilter,
    selectedThreadIds: Set<Long>,
    isSelectionMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (ConversationFilter) -> Unit,
    onConversationClick: (Long, String) -> Unit,
    onToggleSelection: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onTogglePin: (Long, Boolean) -> Unit,
    onArchive: (Long) -> Unit,
    onMarkRead: (Long) -> Unit,
    onMarkUnread: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onBatchDelete: () -> Unit,
    onBatchArchive: () -> Unit,
    onBatchMarkRead: () -> Unit,
    onBatchMarkUnread: () -> Unit,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var activeBottomSheetItem by remember { mutableStateOf<ConversationItem?>(null) }
    var showDeleteConfirmItem by remember { mutableStateOf<ConversationItem?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header / Selection Header
            if (isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                    }
                    Text(
                        text = "${selectedThreadIds.size} selected",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onSelectAll, modifier = Modifier.testTag("action_select_all")) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Select All")
                    }
                    IconButton(onClick = onBatchMarkRead, modifier = Modifier.testTag("action_batch_read")) {
                        Icon(Icons.Default.MarkEmailRead, contentDescription = "Mark Read")
                    }
                    IconButton(onClick = onBatchArchive, modifier = Modifier.testTag("action_batch_archive")) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive")
                    }
                    IconButton(
                        onClick = { showBatchDeleteDialog = true },
                        modifier = Modifier.testTag("action_batch_delete")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SalimDestructive)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Minimal Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("conversations_search_field"),
                        placeholder = { Text("Search messages, contacts, numbers...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Search", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Tabs (All, Unread, Pinned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == ConversationFilter.ALL,
                            onClick = { onFilterChange(ConversationFilter.ALL) },
                            label = { Text("All") },
                            shape = RoundedCornerShape(18.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_all")
                        )

                        FilterChip(
                            selected = selectedFilter == ConversationFilter.UNREAD,
                            onClick = { onFilterChange(ConversationFilter.UNREAD) },
                            label = { Text("Unread") },
                            shape = RoundedCornerShape(18.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_unread")
                        )

                        FilterChip(
                            selected = selectedFilter == ConversationFilter.PINNED,
                            onClick = { onFilterChange(ConversationFilter.PINNED) },
                            label = { Text("Pinned") },
                            shape = RoundedCornerShape(18.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_pinned")
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )

            // Conversation List
            if (conversations.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = if (searchQuery.isNotEmpty()) "No messages found" else "No conversations",
                    description = if (searchQuery.isNotEmpty()) "Try a different search query." else "When you send or receive SMS messages, they will appear here.",
                    actionLabel = if (searchQuery.isEmpty()) "Compose message" else null,
                    onActionClick = onComposeClick,
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(conversations, key = { it.threadId }) { item ->
                        val isSelected = selectedThreadIds.contains(item.threadId)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent)
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            onToggleSelection(item.threadId)
                                        } else {
                                            onConversationClick(item.threadId, item.address)
                                        }
                                    },
                                    onLongClick = {
                                        if (isSelectionMode) {
                                            onToggleSelection(item.threadId)
                                        } else {
                                            activeBottomSheetItem = item
                                        }
                                    }
                                )
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .testTag("conversation_item_${item.threadId}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onToggleSelection(item.threadId) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            AvatarView(
                                displayName = item.displayTitle,
                                photoUri = item.contactPhotoUri,
                                size = 52.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Text(
                                            text = item.displayTitle,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (item.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (item.isPinned) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Pinned",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = DateFormatter.formatConversationDate(item.date),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = if (item.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (item.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.draftSnippet?.let { "Draft: $it" } ?: item.snippet.ifBlank { "No message text" },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            fontWeight = if (item.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                                        ),
                                        color = if (item.draftSnippet != null) SalimDestructive else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (item.unreadCount > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(start = if (isSelectionMode) 124.dp else 86.dp, end = 20.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        // Compose FAB
        if (!isSelectionMode) {
            FloatingActionButton(
                onClick = onComposeClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .size(56.dp)
                    .testTag("conversations_fab_compose")
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Compose Message",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // Contextual Bottom Sheet for Long-pressed Conversation
    activeBottomSheetItem?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { activeBottomSheetItem = null },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = item.displayTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onTogglePin(item.threadId, item.isPinned)
                            activeBottomSheetItem = null
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(if (item.isPinned) "Unpin conversation" else "Pin to top")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (item.unreadCount > 0) onMarkRead(item.threadId) else onMarkUnread(item.threadId)
                            activeBottomSheetItem = null
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (item.unreadCount > 0) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(if (item.unreadCount > 0) "Mark as read" else "Mark as unread")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onArchive(item.threadId)
                            activeBottomSheetItem = null
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Archive conversation")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            activeBottomSheetItem = null
                            showDeleteConfirmItem = item
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = SalimDestructive
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Delete conversation", color = SalimDestructive)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Single Delete Confirmation Dialog
    showDeleteConfirmItem?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmItem = null },
            title = { Text("Delete conversation?") },
            text = { Text("This will permanently remove the conversation with ${item.displayTitle} and its SMS messages.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(item.threadId)
                        showDeleteConfirmItem = null
                    }
                ) {
                    Text("Delete", color = SalimDestructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmItem = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Batch Delete Confirmation Dialog
    if (showBatchDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("Delete selected conversations?") },
            text = { Text("Are you sure you want to permanently delete ${selectedThreadIds.size} conversations from your device?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBatchDelete()
                        showBatchDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = SalimDestructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
