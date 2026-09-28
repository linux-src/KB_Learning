package dev.kbwallet.app.library.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import dev.kbwallet.app.library.domain.LibraryLevel
import dev.kbwallet.app.library.domain.LibraryTopic

@Composable
@ReadOnlyComposable
internal fun LibraryLevel.accent(): Color = when (this) {
    LibraryLevel.BEGINNER -> MaterialTheme.colorScheme.primary
    LibraryLevel.INTERMEDIATE -> MaterialTheme.colorScheme.secondary
    LibraryLevel.ADVANCED -> MaterialTheme.colorScheme.tertiary
}

/** ~200 words per minute, never less than one minute. */
internal fun LibraryTopic.readingMinutes(): Int {
    val words = content.sumOf { p -> p.split(' ', '\n').count { it.isNotBlank() } }
    return (words / 200).coerceAtLeast(1)
}
