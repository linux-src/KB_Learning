package dev.kbwallet.app.trade.presentation.sell

import androidx.compose.ui.Modifier
import dev.kbwallet.app.trade.presentation.common.TradeSuccessOverlay
import dev.kbwallet.app.trade.presentation.common.TradeResult
import dev.kbwallet.app.core.util.formatCoinUnit
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.trade.presentation.common.TradeScreen
import dev.kbwallet.app.trade.presentation.common.TradeType
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.lifecycle.repeatOnLifecycle


@Composable
fun SellScreen(
    coinId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = koinViewModel<SellViewModel>(
        parameters = {
            parametersOf(coinId)
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    var completedSale by remember { mutableStateOf<SellEvents.SellSuccess?>(null) }

    LaunchedEffect(viewModel.events) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is SellEvents.SellSuccess -> completedSale = event
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        TradeScreen(
            state = state,
            tradeType = TradeType.SELL,
            onAmountChange = viewModel::onAmountChanged,
            onPercentageClicked = viewModel::onPercentageClicked,
            onSubmitClicked = viewModel::onSellClicked,
            onToggleMode = viewModel::onToggleMode,
            onBack = onBack,
        )

        val sale = completedSale
        if (sale != null) {
            TradeSuccessOverlay(
                result = TradeResult(
                    isSell = true,
                    coinAmount = formatCoinUnit(sale.amountInUnit, sale.coinSymbol),
                    price = sale.price,
                    total = sale.amountInFiat,
                ),
                onFinished = onSuccess,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
