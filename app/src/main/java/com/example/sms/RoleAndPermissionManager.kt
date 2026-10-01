package com.example.sms

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.core.content.ContextCompat

class RoleAndPermissionManager(private val context: Context) {

    /**
     * Checks if this app is currently the default SMS application on the device.
     */
    fun isDefaultSmsApp(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            val defaultPackage = Telephony.Sms.getDefaultSmsPackage(context)
            defaultPackage == context.packageName
        }
    }

    /**
     * Creates an Intent to prompt the user to make Salim the default SMS app.
     */
    fun createDefaultSmsIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_SMS) == true) {
                roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            } else {
                null
            }
        } else {
            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            }
        }
    }

    /**
     * Check individual permissions.
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasSmsPermissions(): Boolean {
        return isPermissionGranted(android.Manifest.permission.READ_SMS) &&
            isPermissionGranted(android.Manifest.permission.SEND_SMS) &&
            isPermissionGranted(android.Manifest.permission.RECEIVE_SMS)
    }

    fun hasContactsPermission(): Boolean {
        return isPermissionGranted(android.Manifest.permission.READ_CONTACTS)
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionGranted(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
    }

    fun getAllRequiredPermissions(): List<String> {
        val list = mutableListOf(
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        return list
    }
}
