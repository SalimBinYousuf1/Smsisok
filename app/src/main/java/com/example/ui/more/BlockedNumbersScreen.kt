package com.example.ui.more

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BlockedNumberEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SalimTopBar
import com.example.ui.theme.SalimDestructive
import com.example.util.PhoneNumberHelper

@Composable
fun BlockedNumbersScreen(
    blockedNumbers: List<BlockedNumberEntity>,
    onBlockNumber: (String, String) -> Unit,
    onUnblockNumber: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var inputNumber by remember { mutableStateOf("") }
    var inputNote by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SalimTopBar(
                title = "Blocked Numbers",
                onBackClick = onBackClick
            )

            if (blockedNumbers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Block,
                    title = "No blocked numbers",
                    description = "Numbers you block will not be able to send messages to your inbox.",
                    actionLabel = "Add blocked number",
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(blockedNumbers, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                                .testTag("blocked_item_${item.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = SalimDestructive,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = PhoneNumberHelper.formatDisplay(item.rawNumber),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = item.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onUnblockNumber(item.normalizedNumber) },
                                modifier = Modifier.testTag("unblock_button_${item.id}")
                            ) {
                                Text(
                                    text = "Unblock",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(start = 60.dp, end = 20.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Blocked Number
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
                .testTag("fab_add_blocked")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Blocked Number",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                inputNumber = ""
                inputNote = ""
            },
            title = { Text("Block a phone number") },
            text = {
                Column {
                    Text(
                        "Messages from this number will be filtered out.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputNumber,
                        onValueChange = { inputNumber = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+1 (555) 000-0000") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("block_input_number")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputNote,
                        onValueChange = { inputNote = it },
                        label = { Text("Note (optional)") },
                        placeholder = { Text("e.g. Telemarketer") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("block_input_note")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputNumber.isNotBlank()) {
                            onBlockNumber(inputNumber, inputNote)
                            showAddDialog = false
                            inputNumber = ""
                            inputNote = ""
                        }
                    }
                ) {
                    Text("Block", color = SalimDestructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        inputNumber = ""
                        inputNote = ""
                    }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
