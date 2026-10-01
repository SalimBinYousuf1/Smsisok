package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.abs

private val AvatarColors = listOf(
    Color(0xFF5AC8FA), // Light Blue
    Color(0xFF5856D6), // Purple
    Color(0xFFFF2D55), // Pink
    Color(0xFFFF9500), // Orange
    Color(0xFFFFCC00), // Yellow
    Color(0xFF34C759), // Green
    Color(0xFF007AFF), // Blue
    Color(0xFFAF52DE)  // Violet
)

@Composable
fun AvatarView(
    displayName: String?,
    photoUri: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val initials = getInitials(displayName)
    val colorIndex = if (displayName.isNullOrBlank()) 0 else abs(displayName.hashCode()) % AvatarColors.size
    val bgColor = AvatarColors[colorIndex]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = displayName ?: "Contact Photo",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val fontSize = (size.value * 0.42f).sp
            Text(
                text = initials,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun getInitials(name: String?): String {
    if (name.isNullOrBlank()) return "#"
    val parts = name.trim().split(" ", "-", "_").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        parts.isNotEmpty() -> parts[0].take(2).uppercase()
        else -> "#"
    }
}
