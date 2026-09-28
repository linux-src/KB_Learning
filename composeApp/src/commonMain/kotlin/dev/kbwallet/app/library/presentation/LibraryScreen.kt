package dev.kbwallet.app.library.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.library.domain.LibraryContent
import dev.kbwallet.app.library.domain.LibraryLevel
import dev.kbwallet.app.library.domain.LibraryTopic
import dev.kbwallet.app.library.domain.label
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.component.BackHeader
import dev.kbwallet.app.theme.component.KBCard
import dev.kbwallet.app.theme.component.SectionHeader
import dev.kbwallet.app.theme.component.screenContentPadding

@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onTopicClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = appStrings()
    val topicsByLevel = LibraryContent.topics().groupBy { it.level }
    // null = all levels
    var levelFilter by rememberSaveable { mutableStateOf<LibraryLevel?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        BackHeader(title = strings.libraryTitle, subtitle = strings.librarySubtitle, onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = screenContentPadding(top = 0.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                    item {
                        LevelFilterChip(strings.historyFilterAll, levelFilter == null, MaterialTheme.colorScheme.primary) {
                            levelFilter = null
                        }
                    }
                    items(LibraryLevel.entries.filter { topicsByLevel[it].orEmpty().isNotEmpty() }) { level ->
                        LevelFilterChip(level.label(strings), levelFilter == level, level.accent()) {
                            levelFilter = level
                        }
                    }
                }
            }
            LibraryLevel.entries
                .filter { levelFilter == null || it == levelFilter }
                .forEach { level ->
                    val topics = topicsByLevel[level].orEmpty()
                    if (topics.isNotEmpty()) {
                        item(key = "level-$level") {
                            SectionHeader(
                                title = level.label(strings),
                                trailingText = strings.libraryTopicsCount(topics.size),
                                modifier = Modifier.padding(top = Dimens.xs).animateItem(),
                            )
                        }
                        items(topics.withIndex().toList(), key = { it.value.id }) { (index, topic) ->
                            LibraryTopicRow(
                                number = index + 1,
                                topic = topic,
                                onClick = { onTopicClicked(topic.id) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun LevelFilterChip(label: String, selected: Boolean, accent: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accent.copy(alpha = 0.18f),
            selectedLabelColor = accent,
        ),
    )
}

@Composable
private fun LibraryTopicRow(
    number: Int,
    topic: LibraryTopic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = appStrings()
    val accent = topic.level.accent()
    KBCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(Dimens.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.14f)),
            ) {
                Text(number.toString(), style = MaterialTheme.typography.titleMedium, color = accent)
            }
            Spacer(Modifier.width(Dimens.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = topic.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        strings.libraryReadingTime(topic.readingMinutes()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
