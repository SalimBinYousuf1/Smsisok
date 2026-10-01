package com.example.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SalimTopBar
import com.example.ui.settings.DiagnosticsInfo
import com.example.ui.theme.SalimDestructive
import com.example.ui.theme.SalimSuccess

@Composable
fun DiagnosticsScreen(
    diagnostics: DiagnosticsInfo,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SalimTopBar(
            title = "Diagnostics",
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = onRefresh, modifier = Modifier.testTag("button_refresh_diagnostics")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Diagnostics")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "System Telephony & Engine Status",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Real-time state reported directly from Android system providers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        DiagnosticRow("Application", "Salim Native Android SMS")
                        DiagnosticRow("Version", diagnostics.appVersion)
                        DiagnosticRow("Android Version", "${diagnostics.androidVersion} (API ${diagnostics.sdkInt})")
                        DiagnosticRow(
                            "Default SMS Role",
                            if (diagnostics.isDefaultSms) "ACTIVE (Default Handler)" else "INACTIVE",
                            color = if (diagnostics.isDefaultSms) SalimSuccess else SalimDestructive
                        )
                        DiagnosticRow(
                            "SMS Permissions",
                            if (diagnostics.hasSmsPermission) "GRANTED" else "NOT GRANTED",
                            color = if (diagnostics.hasSmsPermission) SalimSuccess else SalimDestructive
                        )
                        DiagnosticRow(
                            "Contacts Permission",
                            if (diagnostics.hasContactsPermission) "GRANTED" else "NOT GRANTED",
                            color = if (diagnostics.hasContactsPermission) SalimSuccess else SalimDestructive
                        )
                        DiagnosticRow(
                            "Notification Permission",
                            if (diagnostics.hasNotificationPermission) "GRANTED" else "NOT GRANTED",
                            color = if (diagnostics.hasNotificationPermission) SalimSuccess else SalimDestructive
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "RADIO & DATA",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        DiagnosticRow("Active SIM Cards", "${diagnostics.simCount}")
                        diagnostics.sims.forEach { sim ->
                            DiagnosticRow("• SIM Slot ${sim.simSlotIndex + 1}", "${sim.displayName} (${sim.carrierName.ifBlank { "Carrier" }})")
                        }
                        DiagnosticRow("Total Conversations", "${diagnostics.totalConversations}")
                        DiagnosticRow("Blocked Numbers", "${diagnostics.totalBlocked}")
                        DiagnosticRow("Storage Mode", "100% On-Device SQLite & Telephony Provider")
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}
