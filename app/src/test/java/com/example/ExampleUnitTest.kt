package com.example

import com.example.util.DateFormatter
import com.example.util.PhoneNumberHelper
import com.example.util.T9Matcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPhoneNumberNormalization() {
        val raw = "+1 (555) 234-5678"
        val normalized = PhoneNumberHelper.normalize(raw)
        assertEquals("+15552345678", normalized)

        val invalid = "abc 123 - 456"
        assertEquals("123456", PhoneNumberHelper.normalize(invalid))
    }

    @Test
    fun testPhoneNumberValidation() {
        assertTrue(PhoneNumberHelper.isValidRecipient("+15551234567"))
        assertTrue(PhoneNumberHelper.isValidRecipient("911"))
        assertTrue(PhoneNumberHelper.isValidRecipient("5551234"))
        assertFalse(PhoneNumberHelper.isValidRecipient(""))
        assertFalse(PhoneNumberHelper.isValidRecipient("   "))
    }

    @Test
    fun testPhoneNumberComparison() {
        assertTrue(PhoneNumberHelper.areEqual("+15552345678", "555-234-5678"))
        assertTrue(PhoneNumberHelper.areEqual("(555) 234-5678", "5552345678"))
        assertFalse(PhoneNumberHelper.areEqual("1234567", "7654321"))
    }

    @Test
    fun testT9KeypadConversion() {
        assertEquals('2', T9Matcher.charToT9Digit('A'))
        assertEquals('2', T9Matcher.charToT9Digit('b'))
        assertEquals('2', T9Matcher.charToT9Digit('C'))
        assertEquals('3', T9Matcher.charToT9Digit('d'))
        assertEquals('7', T9Matcher.charToT9Digit('s'))
        assertEquals('9', T9Matcher.charToT9Digit('Z'))

        assertEquals("72546", T9Matcher.nameToT9("Salim"))
    }

    @Test
    fun testT9Matching() {
        val name = "Salim Malik"
        val phone = "+15552345678"

        // "72546" corresponds to "Salim"
        assertTrue(T9Matcher.matches(name, phone, "72546"))
        // Partial match "72" for "Sa"
        assertTrue(T9Matcher.matches(name, phone, "72"))
        // Match phone digits "555"
        assertTrue(T9Matcher.matches(name, phone, "555"))
        // Match initials "SM" -> "76"
        assertTrue(T9Matcher.matches(name, phone, "76"))

        // Unrelated query
        assertFalse(T9Matcher.matches(name, phone, "99999"))
    }

    @Test
    fun testDateFormatting() {
        val now = System.currentTimeMillis()
        val formatted = DateFormatter.formatConversationDate(now)
        assertTrue(formatted.isNotBlank())
        assertTrue(formatted.contains("AM") || formatted.contains("PM"))
    }
}
