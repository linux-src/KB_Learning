package dev.kbwallet.app.analytics.presentation

import dev.kbwallet.app.theme.component.BackHeader
import dev.kbwallet.app.theme.component.SkeletonList
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import dev.kbwallet.app.theme.component.StatCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PnLScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<PnLViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        BackHeader(title = strings.pnlTitle, onBack = onBack)

        if (state.isLoading) {
            SkeletonList(rows = 4, header = false)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Summary Cards
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                        StatCard(strings.pnlStatTotalTrades, state.totalTrades.toString(), Modifier.weight(1f))
                        StatCard(strings.pnlStatWinRate, state.winRate, Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                        StatCard(strings.pnlStatActiveOrders, state.activeLimitOrders.toString(), Modifier.weight(1f))
                    }
                }

                // Main P&L Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (state.isPnLPositive) LocalKBLearningColorsPalette.current.profitGreen.copy(alpha = 0.1f)
                                else LocalKBLearningColorsPalette.current.lossRed.copy(alpha = 0.1f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(strings.pnlRealized, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.realizedPnL,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isPnLPositive) LocalKBLearningColorsPalette.current.profitGreen
                                else LocalKBLearningColorsPalette.current.lossRed,
                            )
                        }
                    }
                }

                // Detailed stats
                item {
                    Text(strings.pnlDetailedStats, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PnLRow(strings.pnlRowTotalInvested, state.totalInvested)
                        PnLRow(strings.pnlRowTotalRealized, state.totalRealized)
                        PnLRow(strings.pnlRowBestTrade, state.bestTrade, LocalKBLearningColorsPalette.current.profitGreen)
                        PnLRow(strings.pnlRowWorstTrade, state.worstTrade, LocalKBLearningColorsPalette.current.lossRed)
                        PnLRow(strings.pnlRowAvgProfitPerTrade, state.avgProfitPerTrade)
                        PnLRow(strings.pnlRowBuyOrders, state.totalBuy.toString())
                        PnLRow(strings.pnlRowSellOrders, state.totalSell.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun PnLRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onBackground) {
    Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
