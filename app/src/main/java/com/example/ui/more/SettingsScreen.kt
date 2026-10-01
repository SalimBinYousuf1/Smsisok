package com.example.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.repository.SalimSettings
import com.example.data.repository.ThemeMode
import com.example.ui.components.SalimTopBar
import com.example.ui.theme.SalimDestructive

@Composable
fun SettingsScreen(
    settings: SalimSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onEnterKeySendsChange: (Boolean) -> Unit,
    onDeliveryReportsChange: (Boolean) -> Unit,
    onSoundFeedbackChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onConfirmDeleteChange: (Boolean) -> Unit,
    onClearLocalMetadata: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SalimTopBar(
            title = "Settings",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Appearance Section
            item {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Theme Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = settings.themeMode == ThemeMode.LIGHT,
                                onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                                label = { Text("Light") },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f).testTag("theme_chip_light")
                            )

                            FilterChip(
                                selected = settings.themeMode == ThemeMode.DARK,
                                onClick = { onThemeModeChange(ThemeMode.DARK) },
                                label = { Text("Dark") },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f).testTag("theme_chip_dark")
                            )

                            FilterChip(
                                selected = settings.themeMode == ThemeMode.SYSTEM,
                                onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                                label = { Text("System") },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f).testTag("theme_chip_system")
                            )
                        }
                    }
                }
            }

            // Messaging Section
            item {
                Text(
                    text = "MESSAGING BEHAVIOR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingToggleRow(
                            title = "Send with Enter key",
                            desc = "Pressing Enter on your keyboard sends the message immediately",
                            checked = settings.enterKeySends,
                            onCheckedChange = onEnterKeySendsChange,
                            testTag = "toggle_enter_key"
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
                        SettingToggleRow(
                            title = "Delivery reports",
                            desc = "Request delivery confirmation from your mobile carrier",
                            checked = settings.deliveryReports,
                            onCheckedChange = onDeliveryReportsChange,
                            testTag = "toggle_delivery_reports"
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
                        SettingToggleRow(
                            title = "Confirm before deletion",
                            desc = "Ask for confirmation before deleting messages or threads",
                            checked = settings.confirmDelete,
                            onCheckedChange = onConfirmDeleteChange,
                            testTag = "toggle_confirm_delete"
                        )
                    }
                }
            }

            // Sound & Haptics Section
            item {
                Text(
                    text = "FEEDBACK & HAPTICS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingToggleRow(
                            title = "Sound feedback",
                            desc = "Play subtle audio cues when messages are sent or received",
                            checked = settings.soundFeedback,
                            onCheckedChange = onSoundFeedbackChange,
                            testTag = "toggle_sound_feedback"
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
                        SettingToggleRow(
                            title = "Haptic feedback",
                            desc = "Gentle vibration on key taps and action triggers",
                            checked = settings.hapticFeedback,
                            onCheckedChange = onHapticFeedbackChange,
                            testTag = "toggle_haptic_feedback"
                        )
                    }
                }
            }

            // Storage & Local Metadata
            item {
                Text(
                    text = "PRIVACY & STORAGE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearConfirmDialog = true }
                            .padding(18.dp)
                            .testTag("row_clear_metadata"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear Salim Local Cache",
                                fontWeight = FontWeight.SemiBold,
                                color = SalimDestructive,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Clears draft messages and local search queries. System SMS messages are preserved.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Local App Cache?") },
            text = { Text("This will erase unsent drafts and search queries stored locally by Salim. Your system SMS database will not be affected.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearLocalMetadata()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear", color = SalimDestructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
