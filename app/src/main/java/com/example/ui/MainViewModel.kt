package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SalimSettings
import com.example.data.repository.ThemeMode
import com.example.ui.navigation.SalimScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val preferencesRepository = PreferencesRepository(application)

    private val _screenStack = MutableStateFlow<List<SalimScreen>>(listOf(SalimScreen.Home))
    val currentScreen: StateFlow<SalimScreen> = _screenStack.map { it.lastOrNull() ?: SalimScreen.Home }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SalimScreen.Home)

    private val _isDefaultSmsApp = MutableStateFlow(app.roleAndPermissionManager.isDefaultSmsApp())
    val isDefaultSmsApp: StateFlow<Boolean> = _isDefaultSmsApp.asStateFlow()

    private val _hasSmsPermission = MutableStateFlow(app.roleAndPermissionManager.hasSmsPermissions())
    val hasSmsPermission: StateFlow<Boolean> = _hasSmsPermission.asStateFlow()

    val settings: StateFlow<SalimSettings> = preferencesRepository.settings

    val unreadCount: StateFlow<Int> = app.smsRepository.conversations.map { list ->
        list.sumOf { it.unreadCount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun navigateTo(screen: SalimScreen) {
        val currentList = _screenStack.value.toMutableList()
        // If selecting a bottom tab, reset root
        when (screen) {
            SalimScreen.Home,
            SalimScreen.Conversations,
            SalimScreen.Contacts,
            SalimScreen.More -> {
                _screenStack.value = listOf(screen)
            }
            else -> {
                currentList.add(screen)
                _screenStack.value = currentList
            }
        }
    }

    fun navigateBack(): Boolean {
        val currentList = _screenStack.value.toMutableList()
        return if (currentList.size > 1) {
            currentList.removeAt(currentList.lastIndex)
            _screenStack.value = currentList
            true
        } else {
            false
        }
    }

    fun refreshSystemStatus() {
        _isDefaultSmsApp.value = app.roleAndPermissionManager.isDefaultSmsApp()
        _hasSmsPermission.value = app.roleAndPermissionManager.hasSmsPermissions()
        app.smsRepository.refreshConversations()
        app.contactsRepository.refreshContacts()
        app.subscriptionRepository.refreshSubscriptions()
    }

    fun setThemeMode(mode: ThemeMode) {
        preferencesRepository.setThemeMode(mode)
    }

    fun blockNumber(address: String) {
        viewModelScope.launch {
            app.database.salimDao().blockNumber(
                com.example.data.local.BlockedNumberEntity(
                    normalizedNumber = com.example.util.PhoneNumberHelper.normalize(address),
                    rawNumber = address
                )
            )
        }
    }
}
