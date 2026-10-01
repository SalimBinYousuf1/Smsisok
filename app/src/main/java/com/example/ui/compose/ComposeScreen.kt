package com.example.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.data.model.SimInfo
import com.example.ui.components.AvatarView
import com.example.ui.components.SalimTopBar
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.SalimDestructive
import com.example.util.PhoneNumberHelper

@Composable
fun ComposeScreen(
    recipientInput: String,
    selectedContact: ContactItem?,
    messageBody: String,
    contactSuggestions: List<ContactItem>,
    activeSims: List<SimInfo>,
    selectedSimId: Int?,
    isSending: Boolean,
    errorMessage: String?,
    onRecipientChange: (String) -> Unit,
    onContactSelect: (ContactItem) -> Unit,
    onContactRemove: () -> Unit,
    onMessageBodyChange: (String) -> Unit,
    onSimSelect: (Int?) -> Unit,
    onSendMessage: () -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SalimTopBar(
            title = "New Message",
            onBackClick = onBackClick
        )

        // Error Banner
        if (errorMessage != null) {
            Surface(
                color = SalimDestructive.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = errorMessage,
                        color = SalimDestructive,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onClearError) {
                        Text("Dismiss", color = SalimDestructive, fontSize = 12.sp)
                    }
                }
            }
        }

        // Recipient Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "To: ",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (selectedContact != null) {
                InputChip(
                    selected = true,
                    onClick = {},
                    label = { Text("${selectedContact.displayName} (${PhoneNumberHelper.formatDisplay(selectedContact.phoneNumber)})") },
                    trailingIcon = {
                        IconButton(
                            onClick = onContactRemove,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Recipient")
                        }
                    },
                    modifier = Modifier.testTag("selected_recipient_chip")
                )
            } else {
                OutlinedTextField(
                    value = recipientInput,
                    onValueChange = onRecipientChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipient_input_field"),
                    placeholder = { Text("Name or phone number", fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )

        // Contact Suggestions List or Message Composer Area
        Box(modifier = Modifier.weight(1f)) {
            if (contactSuggestions.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(contactSuggestions, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onContactSelect(contact) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("contact_suggestion_${contact.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarView(
                                displayName = contact.displayName,
                                photoUri = contact.photoUri,
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = contact.displayName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = PhoneNumberHelper.formatDisplay(contact.phoneNumber),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 70.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        // Bottom Composer
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Multi-SIM selection if available
                if (activeSims.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SimCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send via: ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        activeSims.forEach { sim ->
                            val isChosen = selectedSimId == sim.subscriptionId || (selectedSimId == null && sim.isDefaultSms)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { onSimSelect(sim.subscriptionId) }
                            ) {
                                Text(
                                    text = sim.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = messageBody,
                        onValueChange = onMessageBodyChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("compose_message_body_field"),
                        placeholder = { Text("Text Message", fontSize = 15.sp) },
                        maxLines = 5,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSend = messageBody.trim().isNotEmpty() &&
                        (selectedContact != null || recipientInput.trim().isNotEmpty()) &&
                        !isSending

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (canSend) SalimBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = canSend) { onSendMessage() }
                            .testTag("compose_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
