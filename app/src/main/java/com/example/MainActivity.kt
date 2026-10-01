package com.example

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.ThemeMode
import com.example.ui.MainViewModel
import com.example.ui.components.SalimBottomBar
import com.example.ui.compose.ComposeScreen
import com.example.ui.compose.ComposeViewModel
import com.example.ui.conversation.ConversationDetailScreen
import com.example.ui.conversation.ConversationDetailViewModel
import com.example.ui.conversations.ConversationsScreen
import com.example.ui.conversations.ConversationsViewModel
import com.example.ui.contacts.ContactsScreen
import com.example.ui.contacts.ContactsViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.more.AboutScreen
import com.example.ui.more.ArchivedScreen
import com.example.ui.more.BlockedNumbersScreen
import com.example.ui.more.ContactDetailScreen
import com.example.ui.more.DefaultRoleScreen
import com.example.ui.more.DiagnosticsScreen
import com.example.ui.more.MoreScreen
import com.example.ui.more.PermissionsScreen
import com.example.ui.more.SettingsScreen
import com.example.ui.navigation.SalimScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.SalimTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val settings by mainViewModel.settings.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemDark
            }

            SalimTheme(darkTheme = darkTheme) {
                SalimApp(
                    mainViewModel = mainViewModel,
                    onRequestDefaultSms = { launchDefaultSmsRoleRequest() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data: Uri? = intent.data

        // Handle SMS notification tap
        val notificationThreadId = intent.getLongExtra("thread_id", -1L)
        val notificationAddress = intent.getStringExtra("address")
        if (notificationThreadId > 0 && !notificationAddress.isNullOrBlank()) {
            mainViewModel.navigateTo(
                SalimScreen.ConversationDetail(notificationThreadId, notificationAddress)
            )
            return
        }

        // Handle SENDTO / SEND sms:, smsto:
        if (Intent.ACTION_SENDTO == action || Intent.ACTION_VIEW == action) {
            val address = data?.schemeSpecificPart?.substringBefore("?") ?: ""
            val body = intent.getStringExtra("sms_body") ?: ""
            if (address.isNotBlank() || body.isNotBlank()) {
                mainViewModel.navigateTo(SalimScreen.Compose(address, body))
            }
        } else if (Intent.ACTION_SEND == action && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            if (sharedText.isNotBlank()) {
                mainViewModel.navigateTo(SalimScreen.Compose(initialBody = sharedText))
            }
        }
    }

    private fun launchDefaultSmsRoleRequest() {
        val app = application as SalimApplication
        val roleIntent = app.roleAndPermissionManager.createDefaultSmsIntent()
        if (roleIntent != null) {
            startActivity(roleIntent)
        }
    }
}

@Composable
fun SalimApp(
    mainViewModel: MainViewModel,
    onRequestDefaultSms: () -> Unit
) {
    val currentScreen by mainViewModel.currentScreen.collectAsState()
    val isDefaultSms by mainViewModel.isDefaultSmsApp.collectAsState()
    val hasSmsPermission by mainViewModel.hasSmsPermission.collectAsState()
    val unreadCount by mainViewModel.unreadCount.collectAsState()

    // Activity Lifecycle Observer to refresh status on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mainViewModel.refreshSystemStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Role launcher
    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        mainViewModel.refreshSystemStatus()
    }

    // Permissions launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        mainViewModel.refreshSystemStatus()
    }

    val requestAllPermissions = {
        val app = SalimApplication.instance
        permissionsLauncher.launch(app.roleAndPermissionManager.getAllRequiredPermissions().toTypedArray())
    }

    // Prompt for permissions on first launch if not granted
    LaunchedEffect(Unit) {
        if (!hasSmsPermission) {
            requestAllPermissions()
        }
    }

    // Back handling
    BackHandler(enabled = currentScreen !is SalimScreen.Home) {
        mainViewModel.navigateBack()
    }

    val isTopLevelScreen = currentScreen is SalimScreen.Home ||
        currentScreen is SalimScreen.Conversations ||
        currentScreen is SalimScreen.Contacts ||
        currentScreen is SalimScreen.More

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelScreen) {
                SalimBottomBar(
                    currentScreen = currentScreen,
                    unreadCount = unreadCount,
                    onTabSelected = { mainViewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is SalimScreen.Home -> {
                    val conversationsVm: ConversationsViewModel = viewModel()
                    val pinned by conversationsVm.pinnedConversations.collectAsState()
                    val allConversations by conversationsVm.conversations.collectAsState()
                    val sims by SalimApplication.instance.subscriptionRepository.activeSims.collectAsState()

                    HomeScreen(
                        isDefaultSms = isDefaultSms,
                        unreadCount = unreadCount,
                        pinnedConversations = pinned,
                        recentConversations = allConversations,
                        activeSims = sims,
                        onConversationClick = { threadId, address ->
                            mainViewModel.navigateTo(SalimScreen.ConversationDetail(threadId, address))
                        },
                        onComposeClick = { mainViewModel.navigateTo(SalimScreen.Compose()) },
                        onSearchClick = { mainViewModel.navigateTo(SalimScreen.Conversations) },
                        onRequestDefaultSms = onRequestDefaultSms,
                        onViewAllConversations = { mainViewModel.navigateTo(SalimScreen.Conversations) }
                    )
                }

                is SalimScreen.Conversations -> {
                    val conversationsVm: ConversationsViewModel = viewModel()
                    val conversationsList by conversationsVm.conversations.collectAsState()
                    val searchQuery by conversationsVm.searchQuery.collectAsState()
                    val selectedFilter by conversationsVm.selectedFilter.collectAsState()
                    val selectedThreadIds by conversationsVm.selectedThreadIds.collectAsState()
                    val isSelectionMode by conversationsVm.isSelectionMode.collectAsState()

                    ConversationsScreen(
                        conversations = conversationsList,
                        searchQuery = searchQuery,
                        selectedFilter = selectedFilter,
                        selectedThreadIds = selectedThreadIds,
                        isSelectionMode = isSelectionMode,
                        onSearchQueryChange = { conversationsVm.setSearchQuery(it) },
                        onFilterChange = { conversationsVm.setFilter(it) },
                        onConversationClick = { threadId, address ->
                            mainViewModel.navigateTo(SalimScreen.ConversationDetail(threadId, address))
                        },
                        onToggleSelection = { conversationsVm.toggleSelection(it) },
                        onSelectAll = { conversationsVm.selectAll() },
                        onClearSelection = { conversationsVm.clearSelection() },
                        onTogglePin = { id, pinned -> conversationsVm.togglePin(id, pinned) },
                        onArchive = { conversationsVm.archiveConversation(it) },
                        onMarkRead = { conversationsVm.markAsRead(it) },
                        onMarkUnread = { conversationsVm.markAsUnread(it) },
                        onDelete = { conversationsVm.deleteConversation(it) },
                        onBatchDelete = { conversationsVm.batchDeleteSelected() },
                        onBatchArchive = { conversationsVm.batchArchiveSelected() },
                        onBatchMarkRead = { conversationsVm.batchMarkReadSelected() },
                        onBatchMarkUnread = { conversationsVm.batchMarkUnreadSelected() },
                        onComposeClick = { mainViewModel.navigateTo(SalimScreen.Compose()) }
                    )
                }

                is SalimScreen.ConversationDetail -> {
                    val detailVm: ConversationDetailViewModel = viewModel(
                        key = "detail_${screen.threadId}",
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                return ConversationDetailViewModel(
                                    SalimApplication.instance,
                                    screen.threadId,
                                    screen.address
                                ) as T
                            }
                        }
                    )
                    val messages by detailVm.messages.collectAsState()
                    val draftText by detailVm.draftText.collectAsState()
                    val contact by detailVm.contact.collectAsState()
                    val activeSims by detailVm.activeSims.collectAsState()
                    val selectedSimId by detailVm.selectedSimId.collectAsState()
                    val isSending by detailVm.isSending.collectAsState()
                    val sendError by detailVm.sendError.collectAsState()

                    ConversationDetailScreen(
                        threadId = screen.threadId,
                        address = screen.address,
                        messages = messages,
                        draftText = draftText,
                        contact = contact,
                        activeSims = activeSims,
                        selectedSimId = selectedSimId,
                        isSending = isSending,
                        sendError = sendError,
                        onDraftChange = { detailVm.updateDraft(it) },
                        onSendMessage = { detailVm.sendMessage() },
                        onSelectSim = { detailVm.setSelectedSim(it) },
                        onDeleteMessage = { detailVm.deleteMessage(it) },
                        onDeleteConversation = {
                            detailVm.deleteConversation {
                                mainViewModel.navigateBack()
                            }
                        },
                        onContactDetailsClick = {
                            mainViewModel.navigateTo(
                                SalimScreen.ContactDetail(screen.address, contact?.displayName)
                            )
                        },
                        onBackClick = { mainViewModel.navigateBack() },
                        onClearError = { detailVm.clearSendError() }
                    )
                }

                is SalimScreen.Compose -> {
                    val composeVm: ComposeViewModel = viewModel(
                        key = "compose_${screen.initialAddress}_${screen.initialBody}",
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                return ComposeViewModel(
                                    SalimApplication.instance,
                                    screen.initialAddress,
                                    screen.initialBody
                                ) as T
                            }
                        }
                    )
                    val recipientInput by composeVm.recipientInput.collectAsState()
                    val selectedContact by composeVm.selectedContact.collectAsState()
                    val messageBody by composeVm.messageBody.collectAsState()
                    val contactSuggestions by composeVm.contactSuggestions.collectAsState()
                    val activeSims by composeVm.activeSims.collectAsState()
                    val selectedSimId by composeVm.selectedSimId.collectAsState()
                    val isSending by composeVm.isSending.collectAsState()
                    val errorMessage by composeVm.errorMessage.collectAsState()

                    ComposeScreen(
                        recipientInput = recipientInput,
                        selectedContact = selectedContact,
                        messageBody = messageBody,
                        contactSuggestions = contactSuggestions,
                        activeSims = activeSims,
                        selectedSimId = selectedSimId,
                        isSending = isSending,
                        errorMessage = errorMessage,
                        onRecipientChange = { composeVm.setRecipientInput(it) },
                        onContactSelect = { composeVm.selectContact(it) },
                        onContactRemove = { composeVm.setRecipientInput("") },
                        onMessageBodyChange = { composeVm.setMessageBody(it) },
                        onSimSelect = { composeVm.setSelectedSim(it) },
                        onSendMessage = {
                            composeVm.sendMessage { destination ->
                                mainViewModel.navigateBack()
                                val threadId = destination.hashCode().toLong()
                                mainViewModel.navigateTo(
                                    SalimScreen.ConversationDetail(threadId, destination)
                                )
                            }
                        },
                        onClearError = { composeVm.clearError() },
                        onBackClick = { mainViewModel.navigateBack() }
                    )
                }

                is SalimScreen.Contacts -> {
                    val contactsVm: ContactsViewModel = viewModel()
                    val contactsList by contactsVm.filteredContacts.collectAsState()
                    val grouped by contactsVm.groupedContacts.collectAsState()
                    val searchQuery by contactsVm.searchQuery.collectAsState()
                    val selectedTab by contactsVm.selectedTab.collectAsState()
                    val isT9Mode by contactsVm.isT9Mode.collectAsState()

                    ContactsScreen(
                        contacts = contactsList,
                        groupedContacts = grouped,
                        searchQuery = searchQuery,
                        selectedTab = selectedTab,
                        isT9Mode = isT9Mode,
                        onSearchChange = { contactsVm.setSearchQuery(it) },
                        onTabChange = { contactsVm.setSelectedTab(it) },
                        onToggleT9 = { contactsVm.toggleT9Mode() },
                        onT9DigitClick = { contactsVm.appendT9Digit(it) },
                        onT9Backspace = { contactsVm.backspaceT9() },
                        onT9Clear = { contactsVm.clearT9() },
                        onContactClick = { address, name ->
                            mainViewModel.navigateTo(SalimScreen.ContactDetail(address, name))
                        },
                        onMessageClick = { address ->
                            mainViewModel.navigateTo(SalimScreen.Compose(initialAddress = address))
                        }
                    )
                }

                is SalimScreen.ContactDetail -> {
                    val contact = remember(screen.address) {
                        SalimApplication.instance.contactsRepository.findContactByAddress(screen.address)
                    }

                    ContactDetailScreen(
                        address = screen.address,
                        contactName = screen.contactName,
                        contact = contact,
                        onMessageClick = { address ->
                            mainViewModel.navigateTo(SalimScreen.Compose(initialAddress = address))
                        },
                        onBlockNumber = { address ->
                            mainViewModel.blockNumber(address)
                        },
                        onBackClick = { mainViewModel.navigateBack() }
                    )
                }

                is SalimScreen.More -> {
                    val settingsVm: SettingsViewModel = viewModel()
                    val archived by settingsVm.archivedConversations.collectAsState()
                    val blocked by settingsVm.blockedNumbers.collectAsState()

                    MoreScreen(
                        isDefaultSms = isDefaultSms,
                        archivedCount = archived.size,
                        blockedCount = blocked.size,
                        onNavigateToArchived = { mainViewModel.navigateTo(SalimScreen.Archived) },
                        onNavigateToBlocked = { mainViewModel.navigateTo(SalimScreen.Blocked) },
                        onNavigateToDefaultRole = { mainViewModel.navigateTo(SalimScreen.DefaultRole) },
                        onNavigateToPermissions = { mainViewModel.navigateTo(SalimScreen.Permissions) },
                        onNavigateToDiagnostics = { mainViewModel.navigateTo(SalimScreen.Diagnostics) },
                        onNavigateToSettings = { mainViewModel.navigateTo(SalimScreen.Settings) },
                        onNavigateToAbout = { mainViewModel.navigateTo(SalimScreen.About) }
                    )
                }

                is SalimScreen.Archived -> {
                    val settingsVm: SettingsViewModel = viewModel()
                    val archived by settingsVm.archivedConversations.collectAsState()

                    ArchivedScreen(
                        archivedConversations = archived,
                        onUnarchive = { settingsVm.unarchiveConversation(it) },
                        onConversationClick = { threadId, address ->
                            mainViewModel.navigateTo(SalimScreen.ConversationDetail(threadId, address))
                        },
                        onBackClick = { mainViewModel.navigateBack() }
                    )
                }

                is SalimScreen.Blocked -> {
                    val settingsVm: SettingsViewModel = viewModel()
                    val blocked by settingsVm.blockedNumbers.collectAsState()

                    BlockedNumbersScreen(
                        blockedNumbers = blocked,
                        onBlockNumber = { number, note -> settingsVm.blockNumber(number, note) },
                        onUnblockNumber = { settingsVm.unblockNumber(it) },
                        onBackClick = { mainViewModel.navigateBack() }
                    )
                }

                is SalimScreen.DefaultRole -> {
                    DefaultRoleScreen(
                        isDefaultSms = isDefaultSms,
                        onRequestDefaultSms = onRequestDefaultSms,
                        onBackClick = { mainViewModel.navigateBack() }
                    )
                }

                is SalimScreen.Permissions -> {
                    val roleManager = SalimApplication.instance.roleAndPermissionManager

                    PermissionsScreen(
                        hasSmsPermission = roleManager.hasSmsPermissions(),
                        hasContactsPermission = roleManager.hasContactsPermission(),
                        hasNotificationPermission = roleManager.hasNotificationPermission(),
                        onRequestPermissions = requestAllPermissions,
                        onBackClick = { mainViewModel.navigateBack() }
                    )
                }

                is SalimScreen.Diagnostics -> {
                    val settingsVm: SettingsViewModel = viewModel()
                    val diagnostics by settingsVm.diagnostics.collectAsState()

                    DiagnosticsScreen(
                        diagnostics = diagnostics,
                        onRefresh = { settingsVm.refreshDiagnostics() },
                        onBackClick = { mainViewModel.navigateBack() }
                    )
                }

                is SalimScreen.Settings -> {
                    val settingsVm: SettingsViewModel = viewModel()
                    val settings by settingsVm.settings.collectAsState()

                    SettingsScreen(
                        settings = settings,
                        onThemeModeChange = { settingsVm.setThemeMode(it) },
                        onEnterKeySendsChange = { settingsVm.setEnterKeySends(it) },
                        onDeliveryReportsChange = { settingsVm.setDeliveryReports(it) },
                        onSoundFeedbackChange = { settingsVm.setSoundFeedback(it) },
                        onHapticFeedbackChange = { settingsVm.setHapticFeedback(it) },
                        onConfirmDeleteChange = { settingsVm.setConfirmDelete(it) },
                        onClearLocalMetadata = { settingsVm.clearLocalMetadata() },
                        onBackClick = { mainViewModel.navigateBack() }
                    )
                }

                is SalimScreen.About -> {
                    AboutScreen(onBackClick = { mainViewModel.navigateBack() })
                }
            }
        }
    }
}
