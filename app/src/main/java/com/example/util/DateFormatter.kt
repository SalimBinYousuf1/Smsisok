package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {

    fun formatConversationDate(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val now = Calendar.getInstance()
        val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp }

        // If today, format as "9:41 AM"
        if (now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)
        ) {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        // If yesterday
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (yesterday.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Yesterday"
        }

        // If within last 6 days, show day name: "Monday"
        val sixDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
        if (msgTime.after(sixDaysAgo)) {
            val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        // If within current year, show "Oct 1"
        if (now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)) {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        // Older, show "MM/dd/yy"
        val sdf = SimpleDateFormat("M/d/yy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatMessageDetailTime(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatMessageHeaderDate(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val now = Calendar.getInstance()
        val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp }

        if (now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Today " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (yesterday.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Yesterday " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }

        val sdf = SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
