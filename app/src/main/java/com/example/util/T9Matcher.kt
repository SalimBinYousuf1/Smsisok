package com.example.util

object T9Matcher {

    /**
     * Maps an alphanumeric character to its classic telephone keypad digit.
     * 2 = ABC, 3 = DEF, 4 = GHI, 5 = JKL, 6 = MNO, 7 = PQRS, 8 = TUV, 9 = WXYZ
     */
    fun charToT9Digit(c: Char): Char? {
        return when (c.lowercaseChar()) {
            'a', 'b', 'c' -> '2'
            'd', 'e', 'f' -> '3'
            'g', 'h', 'i' -> '4'
            'j', 'k', 'l' -> '5'
            'm', 'n', 'o' -> '6'
            'p', 'q', 'r', 's' -> '7'
            't', 'u', 'v' -> '8'
            'w', 'x', 'y', 'z' -> '9'
            '0' -> '0'
            '1' -> '1'
            '2' -> '2'
            '3' -> '3'
            '4' -> '4'
            '5' -> '5'
            '6' -> '6'
            '7' -> '7'
            '8' -> '8'
            '9' -> '9'
            else -> null
        }
    }

    /**
     * Converts a name to its T9 digit string.
     */
    fun nameToT9(name: String): String {
        val sb = StringBuilder()
        for (ch in name) {
            val digit = charToT9Digit(ch)
            if (digit != null) {
                sb.append(digit)
            }
        }
        return sb.toString()
    }

    /**
     * Checks if a contact name or phone number matches a given T9 query string.
     */
    fun matches(name: String, phoneNumber: String, t9Query: String): Boolean {
        if (t9Query.isBlank()) return true
        val cleanQuery = t9Query.trim()

        // 1. Check if the normalized phone number contains the query digits
        val normPhone = PhoneNumberHelper.normalize(phoneNumber)
        if (normPhone.contains(cleanQuery)) {
            return true
        }

        // 2. Check if the name in T9 matches
        val nameT9 = nameToT9(name)
        if (nameT9.contains(cleanQuery)) {
            return true
        }

        // 3. Check for initials matching (e.g. "JD" -> "53")
        val words = name.split(" ", "-", "_").filter { it.isNotBlank() }
        val initialsT9 = words.mapNotNull { it.firstOrNull()?.let { c -> charToT9Digit(c) } }.joinToString("")
        if (initialsT9.startsWith(cleanQuery)) {
            return true
        }

        return false
    }
}
