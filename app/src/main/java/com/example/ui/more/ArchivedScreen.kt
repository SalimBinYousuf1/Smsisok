package com.example.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationItem
import com.example.ui.components.AvatarView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SalimTopBar
import com.example.util.DateFormatter

@Composable
fun ArchivedScreen(
    archivedConversations: List<ConversationItem>,
    onUnarchive: (Long) -> Unit,
    onConversationClick: (Long, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SalimTopBar(
            title = "Archived",
            onBackClick = onBackClick
        )

        if (archivedConversations.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Archive,
                title = "No archived conversations",
                description = "Conversations you archive will be kept here separate from your main inbox.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(archivedConversations, key = { it.threadId }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConversationClick(item.threadId, item.address) }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .testTag("archived_item_${item.threadId}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarView(
                            displayName = item.displayTitle,
                            photoUri = item.contactPhotoUri,
                            size = 48.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.displayTitle,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = DateFormatter.formatConversationDate(item.date),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.snippet.ifBlank { "No messages" },
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onUnarchive(item.threadId) },
                            modifier = Modifier.testTag("unarchive_button_${item.threadId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Unarchive,
                                contentDescription = "Unarchive",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 82.dp, end = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}
