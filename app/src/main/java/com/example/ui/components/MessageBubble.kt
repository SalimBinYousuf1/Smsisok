package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmsMessageItem
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimBubbleIncoming
import com.example.ui.theme.SalimBubbleOutgoing
import com.example.ui.theme.SalimDarkBubbleIncoming
import com.example.ui.theme.SalimDarkBubbleOutgoing
import com.example.ui.theme.SalimDestructive
import com.example.util.DateFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: SmsMessageItem,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    onRetryClick: () -> Unit = {}
) {
    val isOutgoing = message.isOutgoing
    val alignment = if (isOutgoing) Alignment.End else Alignment.Start

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val bubbleColor = when {
        isOutgoing -> if (isDarkTheme) SalimDarkBubbleOutgoing else SalimBubbleOutgoing
        else -> if (isDarkTheme) SalimDarkBubbleIncoming else SalimBubbleIncoming
    }

    val textColor = when {
        isOutgoing -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .testTag("message_bubble_${message.id}")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = message.body,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateFormatter.formatConversationDate(message.date),
                        color = if (isOutgoing) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )

                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when {
                            message.isFailed -> {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Failed",
                                    tint = SalimDestructive,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            message.isPending -> {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Sending",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (message.isFailed) {
            Text(
                text = "Failed to send • Tap to retry",
                color = SalimDestructive,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(top = 2.dp, end = 4.dp)
                    .combinedClickable(onClick = onRetryClick)
            )
        }
    }
}
