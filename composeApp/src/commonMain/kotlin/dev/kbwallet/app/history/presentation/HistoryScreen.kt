package dev.kbwallet.app.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.AppStrings
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.component.EmptyState
import dev.kbwallet.app.theme.component.IconBadge
import dev.kbwallet.app.theme.component.KBCard
import dev.kbwallet.app.theme.component.ScreenTitle
import dev.kbwallet.app.theme.component.SkeletonList
import dev.kbwallet.app.theme.component.StatCard
import dev.kbwallet.app.theme.component.Tag
import dev.kbwallet.app.theme.component.screenContentPadding
import dev.kbwallet.app.theme.tabular
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DateTimeUnit
import org.koin.compose.viewmodel.koinViewModel

private enum class TxFilter { All, Buy, Sell }

@Composable
fun HistoryScreen() {
    val viewModel = koinViewModel<HistoryViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        SkeletonList(rows = 6)
    } else {
        HistoryContent(
            state = state,
            onEditNote = viewModel::startEditing,
        )
    }

    if (state.editingTransactionId != null) {
        JournalDialog(
            notes = state.editNotes,
            tags = state.editTags,
            onNotesChange = viewModel::onEditNotesChanged,
            onTagsChange = viewModel::onEditTagsChanged,
            onSave = viewModel::saveJournalEntry,
            onDismiss = viewModel::cancelEditing,
        )
    }
}

@Composable
private fun HistoryContent(
    state: HistoryState,
    onEditNote: (Long) -> Unit,
) {
    val strings = appStrings()
    var filter by rememberSaveable { mutableStateOf(TxFilter.All) }

    val visible = state.transactions.filter {
        when (filter) {
            TxFilter.All -> true
            TxFilter.Buy -> it.type == "BUY"
            TxFilter.Sell -> it.type != "BUY"
        }
    }
    val tz = TimeZone.currentSystemDefault()
    val grouped = visible
        .sortedByDescending { it.timestamp }
        .groupBy { Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(tz).date }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(),
        verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
    ) {
        item {
            ScreenTitle(title = strings.historyTitle)
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max).padding(top = Dimens.xs)
            ) {
                StatCard(
                    title = strings.historyStatTotalTrades,
                    value = state.totalTrades.toString(),
                    modifier = Modifier.weight(1f),
                )
                // Buy/sell are neutral actions, so no profit/loss colours here.
                StatCard(
                    title = strings.historyStatTotalBuy,
                    value = state.totalBuy.toString(),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary,
                )
                StatCard(
                    title = strings.historyStatTotalSell,
                    value = state.totalSell.toString(),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        if (state.transactions.isNotEmpty()) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
                    modifier = Modifier.padding(top = Dimens.xs),
                ) {
                    items(TxFilter.entries) { f ->
                        FilterChip(
                            selected = filter == f,
                            onClick = { filter = f },
                            label = {
                                Text(
                                    when (f) {
                                        TxFilter.All -> strings.historyFilterAll
                                        TxFilter.Buy -> strings.historyBuyLabel
                                        TxFilter.Sell -> strings.historySellLabel
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }
        }

        if (visible.isEmpty()) {
            item {
                KBCard(modifier = Modifier.fillMaxWidth()) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = strings.historyEmptyTitle,
                        subtitle = strings.historyEmptySubtitle,
                    )
                }
            }
        } else {
            grouped.forEach { (date, transactions) ->
                item(key = "header-$date") {
                    Text(
                        text = dayLabel(date, strings),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimens.sm, start = Dimens.xxs).animateItem(),
                    )
                }
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onEditNote = { onEditNote(transaction.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

private fun dayLabel(date: LocalDate, strings: AppStrings): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (date) {
        today -> strings.historyToday
        today.minus(1, DateTimeUnit.DAY) -> strings.historyYesterday
        else -> buildString {
            append(date.dayOfMonth).append(' ').append(strings.monthsShort[date.monthNumber - 1])
            if (date.year != today.year) append(' ').append(date.year)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionItem(
    transaction: TransactionUiModel,
    onEditNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = appStrings()
    val isBuy = transaction.type == "BUY"
    val accent = if (isBuy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    KBCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = Dimens.md, end = Dimens.xs, top = Dimens.sm, bottom = Dimens.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = if (isBuy) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                tint = accent,
                shape = androidx.compose.foundation.shape.CircleShape,
            )
            Spacer(modifier = Modifier.width(Dimens.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${if (isBuy) strings.historyBuyLabel else strings.historySellLabel} ${transaction.coinName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${transaction.formattedTime} · ${strings.historyPriceLabel(transaction.formattedPrice)}",
                    style = MaterialTheme.typography.bodySmall.tabular(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = Dimens.xs)) {
                // Outgoing cash is neutral, incoming cash is highlighted.
                Text(
                    text = "${if (isBuy) "−" else "+"}${transaction.formattedFiatAmount}",
                    style = MaterialTheme.typography.titleSmall.tabular(),
                    color = if (isBuy) MaterialTheme.colorScheme.onSurface else KBTheme.colors.profitGreen,
                )
                Text(
                    text = "${if (isBuy) "+" else "−"}${transaction.formattedUnitAmount}",
                    style = MaterialTheme.typography.bodySmall.tabular(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (transaction.notes.isNotEmpty() || transaction.tags.isNotEmpty()) {
            Spacer(Modifier.height(Dimens.xs))
            Row(Modifier.padding(start = 52.dp, end = Dimens.xs)) {
                Icon(
                    Icons.AutoMirrored.Filled.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp),
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    if (transaction.notes.isNotEmpty()) {
                        Text(
                            text = transaction.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (transaction.tags.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            transaction.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                                Tag("#$it", color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 52.dp, top = Dimens.xxs),
        ) {
            Tag(strings.historyStatus(transaction.status), color = KBTheme.colors.profitGreen)
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = onEditNote,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    if (transaction.notes.isEmpty() && transaction.tags.isEmpty()) strings.historyJournalAdd
                    else strings.historyJournalEdit,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun JournalDialog(
    notes: String,
    tags: String,
    onNotesChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = appStrings()
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
        title = { Text(strings.historyJournalTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text(strings.historyJournalNotesLabel) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = onTagsChange,
                    label = { Text(strings.historyJournalTagsLabel) },
                    supportingText = { Text(strings.historyJournalTagsHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text(strings.actionSave) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.actionCancel) } },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
