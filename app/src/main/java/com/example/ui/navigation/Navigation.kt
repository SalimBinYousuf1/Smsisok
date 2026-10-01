package com.example.ui.navigation

sealed class SalimScreen {
    // Top-level bottom destinations
    object Home : SalimScreen()
    object Conversations : SalimScreen()
    object Contacts : SalimScreen()
    object More : SalimScreen()

    // Sub-screens
    data class ConversationDetail(val threadId: Long, val address: String) : SalimScreen()
    data class Compose(val initialAddress: String = "", val initialBody: String = "") : SalimScreen()
    data class ContactDetail(val address: String, val contactName: String? = null) : SalimScreen()
    object Archived : SalimScreen()
    object Blocked : SalimScreen()
    object DefaultRole : SalimScreen()
    object Permissions : SalimScreen()
    object Diagnostics : SalimScreen()
    object Settings : SalimScreen()
    object About : SalimScreen()
}
