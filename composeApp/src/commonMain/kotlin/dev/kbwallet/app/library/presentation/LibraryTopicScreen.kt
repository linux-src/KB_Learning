package dev.kbwallet.app.library.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.library.domain.LibraryContent
import dev.kbwallet.app.library.domain.label
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.component.BackHeader
import dev.kbwallet.app.theme.component.EmptyState
import dev.kbwallet.app.theme.component.IconBadge
import dev.kbwallet.app.theme.component.KBCard
import dev.kbwallet.app.theme.component.Tag
import androidx.compose.material.icons.automirrored.filled.MenuBook

@Composable
fun LibraryTopicScreen(
    topicId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTopicClicked: (String) -> Unit = {},
) {
    val strings = appStrings()
    val topics = LibraryContent.topics()
    val index = topics.indexOfFirst { it.id == topicId }
    val topic = topics.getOrNull(index)
    val next = topics.getOrNull(index + 1)
    val listState = rememberLazyListState()

    val progress by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            if (total == 0) 0f else {
                val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                ((last + 1).toFloat() / total).coerceIn(0f, 1f)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        BackHeader(title = strings.libraryTitle, onBack = onBack)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = topic?.level?.accent() ?: MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Butt,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )

        if (topic == null) {
            EmptyState(icon = Icons.AutoMirrored.Filled.MenuBook, title = strings.libraryTopicNotFound)
            return
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                state = listState,
                // Keep line length readable on wide windows.
                modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Dimens.screenPadding, vertical = Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Tag(topic.level.label(strings), color = topic.level.accent())
                        Spacer(Modifier.width(Dimens.sm))
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            strings.libraryReadingTime(topic.readingMinutes()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(Dimens.sm))
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(Dimens.xs))
                    Text(
                        text = topic.summary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(topic.content) { paragraph ->
                    Text(
                        text = paragraph,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.92f),
                        lineHeight = 26.sp,
                    )
                }
                item {
                    Spacer(Modifier.height(Dimens.sm))
                    if (next != null) {
                        KBCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onTopicClicked(next.id) },
                            contentPadding = PaddingValues(Dimens.lg),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        strings.libraryNextTopic,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = next.level.accent(),
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        next.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                IconBadge(Icons.AutoMirrored.Filled.ArrowForward, tint = next.level.accent())
                            }
                        }
                    } else {
                        KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Dimens.lg)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconBadge(Icons.Default.CheckCircle)
                                Spacer(Modifier.width(Dimens.md))
                                Text(
                                    strings.libraryFinished,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Dimens.xl))
                }
            }
        }
    }
}
