package com.example.util

import android.telephony.PhoneNumberUtils
import java.util.Locale

object PhoneNumberHelper {

    /**
     * Normalizes a phone number by stripping spaces, dashes, parentheses, and dots.
     */
    fun normalize(number: String?): String {
        if (number.isNullOrBlank()) return ""
        val digits = StringBuilder()
        for (ch in number) {
            if (ch.isDigit() || ch == '+') {
                digits.append(ch)
            }
        }
        return digits.toString()
    }

    /**
     * Formats a phone number for clean UI display.
     */
    fun formatDisplay(number: String?): String {
        if (number.isNullOrBlank()) return ""
        return try {
            PhoneNumberUtils.formatNumber(number, Locale.getDefault().country) ?: number
        } catch (_: Throwable) {
            number
        }
    }

    /**
     * Compares two phone numbers to see if they refer to the same recipient,
     * accounting for country codes or national formatting.
     */
    fun areEqual(num1: String?, num2: String?): Boolean {
        if (num1.isNullOrBlank() || num2.isNullOrBlank()) return false
        val norm1 = normalize(num1)
        val norm2 = normalize(num2)
        if (norm1 == norm2) return true
        if (norm1.length >= 7 && norm2.length >= 7 && norm1.takeLast(7) == norm2.takeLast(7)) return true
        return try {
            PhoneNumberUtils.compare(norm1, norm2)
        } catch (_: Throwable) {
            norm1.endsWith(norm2) || norm2.endsWith(norm1)
        }
    }

    /**
     * Validates whether an address is a valid phone number or shortcode recipient.
     */
    fun isValidRecipient(address: String?): Boolean {
        if (address.isNullOrBlank()) return false
        val trimmed = address.trim()
        val normalized = normalize(trimmed)
        return normalized.length >= 3 && (normalized.all { it.isDigit() || it == '+' })
    }
}
