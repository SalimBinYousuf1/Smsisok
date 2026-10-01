package com.example.ui.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.ui.components.AvatarView
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.SalimBlue
import com.example.util.PhoneNumberHelper

@Composable
fun ContactsScreen(
    contacts: List<ContactItem>,
    groupedContacts: Map<Char, List<ContactItem>>,
    searchQuery: String,
    selectedTab: ContactsTab,
    isT9Mode: Boolean,
    onSearchChange: (String) -> Unit,
    onTabChange: (ContactsTab) -> Unit,
    onToggleT9: () -> Unit,
    onT9DigitClick: (Char) -> Unit,
    onT9Backspace: () -> Unit,
    onT9Clear: () -> Unit,
    onContactClick: (String, String) -> Unit,
    onMessageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contacts",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onToggleT9,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isT9Mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("toggle_t9_button")
                ) {
                    Icon(
                        imageVector = if (isT9Mode) Icons.Default.Keyboard else Icons.Default.Dialpad,
                        contentDescription = "Toggle T9 Keypad",
                        tint = if (isT9Mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("contacts_search_field"),
                placeholder = {
                    Text(
                        if (isT9Mode) "T9 Query: $searchQuery" else "Search contacts...",
                        fontSize = 14.sp
                    )
                },
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
                        IconButton(onClick = onT9Clear) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
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

            Spacer(modifier = Modifier.height(8.dp))

            // Tab chips (All, Favorites)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == ContactsTab.ALL,
                    onClick = { onTabChange(ContactsTab.ALL) },
                    label = { Text("All Contacts") },
                    shape = RoundedCornerShape(18.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("tab_all_contacts")
                )

                FilterChip(
                    selected = selectedTab == ContactsTab.FAVORITES,
                    onClick = { onTabChange(ContactsTab.FAVORITES) },
                    label = { Text("Favorites") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == ContactsTab.FAVORITES) Color.White else MaterialTheme.colorScheme.primary
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("tab_favorites_contacts")
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )

        // T9 Keypad View if enabled
        if (isT9Mode) {
            T9Keypad(
                onDigitClick = onT9DigitClick,
                onBackspace = onT9Backspace,
                onClear = onT9Clear
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
        }

        // Contact List
        if (contacts.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.People,
                title = if (selectedTab == ContactsTab.FAVORITES) "No favorite contacts" else "No contacts found",
                description = if (selectedTab == ContactsTab.FAVORITES) "Contacts you star will appear here." else "No contacts match the current query.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                groupedContacts.forEach { (initial, list) ->
                    item(key = "header_$initial") {
                        Text(
                            text = initial.toString(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }

                    items(list, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onContactClick(contact.phoneNumber, contact.displayName) }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                                .testTag("contact_item_${contact.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarView(
                                displayName = contact.displayName,
                                photoUri = contact.photoUri,
                                size = 44.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = PhoneNumberHelper.formatDisplay(contact.phoneNumber),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Quick Message Button
                            IconButton(
                                onClick = { onMessageClick(contact.phoneNumber) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .size(36.dp)
                                    .testTag("contact_msg_button_${contact.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "Message",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Quick Call Button
                            IconButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .size(36.dp)
                                    .testTag("contact_call_button_${contact.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(start = 78.dp, end = 20.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun T9Keypad(
    onDigitClick: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keypad = listOf(
        listOf(Pair('1', ""), Pair('2', "ABC"), Pair('3', "DEF")),
        listOf(Pair('4', "GHI"), Pair('5', "JKL"), Pair('6', "MNO")),
        listOf(Pair('7', "PQRS"), Pair('8', "TUV"), Pair('9', "WXYZ")),
        listOf(Pair('*', ""), Pair('0', "+"), Pair('#', ""))
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 32.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keypad.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { (digit, letters) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDigitClick(digit) }
                            .testTag("t9_key_$digit"),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = digit.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (letters.isNotEmpty()) {
                                Text(
                                    text = letters,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action row: Clear & Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear", color = SalimBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            IconButton(
                onClick = onBackspace,
                modifier = Modifier
                    .weight(1f)
                    .testTag("t9_backspace")
            ) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
