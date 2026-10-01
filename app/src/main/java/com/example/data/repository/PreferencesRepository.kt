package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class SalimSettings(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val enterKeySends: Boolean = false,
    val deliveryReports: Boolean = true,
    val soundFeedback: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true,
    val preferredSimId: Int = -1
)

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("salim_preferences", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<SalimSettings> = _settings.asStateFlow()

    private fun readSettings(): SalimSettings {
        val themeStr = prefs.getString("theme_mode", ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        val theme = try { ThemeMode.valueOf(themeStr) } catch (_: Exception) { ThemeMode.LIGHT }
        return SalimSettings(
            themeMode = theme,
            enterKeySends = prefs.getBoolean("enter_key_sends", false),
            deliveryReports = prefs.getBoolean("delivery_reports", true),
            soundFeedback = prefs.getBoolean("sound_feedback", true),
            hapticFeedback = prefs.getBoolean("haptic_feedback", true),
            confirmDelete = prefs.getBoolean("confirm_delete", true),
            preferredSimId = prefs.getInt("preferred_sim_id", -1)
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setEnterKeySends(enabled: Boolean) {
        prefs.edit().putBoolean("enter_key_sends", enabled).apply()
        _settings.value = _settings.value.copy(enterKeySends = enabled)
    }

    fun setDeliveryReports(enabled: Boolean) {
        prefs.edit().putBoolean("delivery_reports", enabled).apply()
        _settings.value = _settings.value.copy(deliveryReports = enabled)
    }

    fun setSoundFeedback(enabled: Boolean) {
        prefs.edit().putBoolean("sound_feedback", enabled).apply()
        _settings.value = _settings.value.copy(soundFeedback = enabled)
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
        _settings.value = _settings.value.copy(hapticFeedback = enabled)
    }

    fun setConfirmDelete(enabled: Boolean) {
        prefs.edit().putBoolean("confirm_delete", enabled).apply()
        _settings.value = _settings.value.copy(confirmDelete = enabled)
    }

    fun setPreferredSimId(subId: Int) {
        prefs.edit().putInt("preferred_sim_id", subId).apply()
        _settings.value = _settings.value.copy(preferredSimId = subId)
    }
}
