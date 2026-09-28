package dev.kbwallet.app.trade.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.semantics.Role
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.tabular
import dev.kbwallet.app.theme.component.CoinAvatar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import dev.kbwallet.app.trade.presentation.common.component.rememberCurrencyVisualTransformation
import org.jetbrains.compose.resources.stringResource
import dev.kbwallet.app.core.util.formatFiat

@Composable
fun TradeScreen(
    state: TradeState,
    tradeType: TradeType,
    onAmountChange: (String) -> Unit,
    onPercentageClicked: (Double) -> Unit,
    onSubmitClicked: () -> Unit,
    onToggleMode: () -> Unit,
    onBack: () -> Unit = {},
) {
    val strings = appStrings()
    val accentColor = when (tradeType) {
        TradeType.BUY -> KBTheme.colors.profitGreen
        TradeType.SELL -> KBTheme.colors.lossRed
    }
    val buttonTextColor = when (tradeType) {
        TradeType.BUY -> MaterialTheme.colorScheme.onPrimary
        TradeType.SELL -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ── Close ──
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = Dimens.sm),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Icon(Icons.Default.Close, contentDescription = strings.actionBack)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.xs, bottom = Dimens.xl, start = Dimens.xl, end = Dimens.xl)
        ) {
            // ── Coin ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinAvatar(
                    iconUrl = state.coin?.iconUrl,
                    symbol = state.coin?.symbol ?: "?",
                    size = 32.dp,
                )
                Spacer(modifier = Modifier.width(Dimens.xs))
                Text(
                    text = state.coin?.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("trade_screen_coin_name"),
                )
            }
            if (state.coin != null) {
                Text(
                    text = "1 ${state.coin.symbol.uppercase()} ≈ ${formatFiat(state.coin.price)}",
                    style = MaterialTheme.typography.bodySmall.tabular(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.xxs)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.lg))

            // ── Mode Toggle (Fiat / Coin) ──
            UnitToggle(
                fiatLabel = "USD",
                coinLabel = (state.coin?.symbol ?: strings.tradeCoinFallback).uppercase(),
                isCoin = state.isAmountInUnits,
                accent = accentColor,
                onToggle = onToggleMode,
            )

            Spacer(modifier = Modifier.height(Dimens.lg))

            // ── Title ──
            Text(
                text = when (tradeType) {
                    TradeType.BUY -> if (state.isAmountInUnits) strings.tradeCoinAmountLabel else strings.tradeBuyAmountLabel
                    TradeType.SELL -> if (state.isAmountInUnits) strings.tradeCoinAmountLabel else strings.tradeSellAmountLabel
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // ── Amount Input ──
            CenteredDollarTextField(
                amountText = state.amount,
                onAmountChange = onAmountChange
            )

            // ── Fiat equivalent (when in coin mode) ──
            if (state.isAmountInUnits && state.fiatEquivalent.isNotEmpty()) {
                Text(
                    text = state.fiatEquivalent,
                    style = MaterialTheme.typography.bodyMedium.tabular(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(Dimens.xs))

            // ── Available Balance ──
            Text(
                text = "${strings.tradeAvailablePrefix}${state.availableAmount}",
                style = MaterialTheme.typography.labelMedium.tabular(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )

            // ── Error ──
            AnimatedVisibility(visible = state.error != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = Dimens.sm)
                        .clip(RoundedCornerShape(Dimens.controlRadius))
                        .background(KBTheme.colors.lossContainer)
                        .padding(horizontal = Dimens.sm, vertical = Dimens.xs),
                ) {
                    Icon(Icons.Default.ErrorOutline, null, tint = KBTheme.colors.lossRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = state.error?.let { stringResource(it) }.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("trade_error")
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.lg))

            // ── Percentage Chips ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(0.25 to "25%", 0.5 to "50%", 0.75 to "75%", 1.0 to "MAX").forEach { (frac, label) ->
                    FilledTonalButton(
                        onClick = { onPercentageClicked(frac) },
                        modifier = Modifier.weight(1f).height(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(Dimens.chipRadius),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.lg))

            // ── Action Button ──
            Button(
                onClick = onSubmitClicked,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = buttonTextColor,
                    disabledContainerColor = accentColor.copy(alpha = 0.5f),
                    disabledContentColor = buttonTextColor.copy(alpha = 0.5f)
                ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = buttonTextColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = when (tradeType) {
                            TradeType.BUY -> strings.tradeBuyButton
                            TradeType.SELL -> strings.tradeSellButton
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitToggle(
    fiatLabel: String,
    coinLabel: String,
    isCoin: Boolean,
    accent: Color,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
    ) {
        listOf(false to fiatLabel, true to coinLabel).forEach { (coinSegment, label) ->
            val selected = coinSegment == isCoin
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable(enabled = !selected, role = Role.Tab, onClick = onToggle)
                    .padding(horizontal = Dimens.md, vertical = Dimens.xs),
            )
        }
    }
}

@Composable
fun CenteredDollarTextField(
    modifier: Modifier = Modifier,
    amountText: String,
    onAmountChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val currencyVisualTransformation = rememberCurrencyVisualTransformation()

    val displayText = amountText.trimStart('$')

    BasicTextField(
        value = displayText,
        onValueChange = { newValue ->
            val cleaned = newValue.filter { it.isDigit() || it == '.' }
                .let { str ->
                    val dotIndex = str.indexOf('.')
                    if (dotIndex >= 0) {
                        str.take(dotIndex + 1) + str.drop(dotIndex + 1).filter { it.isDigit() }
                    } else str
                }

            if (cleaned.isEmpty()) {
                onAmountChange("")
                return@BasicTextField
            }

            val num = cleaned.toDoubleOrNull() ?: return@BasicTextField
            onAmountChange(cleaned)
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .padding(16.dp),
        textStyle = MaterialTheme.typography.displaySmall.tabular().copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 40.sp,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(64.dp).wrapContentWidth()
            ) {
                if (displayText.isEmpty()) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.displaySmall.tabular(),
                        fontSize = 40.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
                innerTextField()
            }
        },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        visualTransformation = currencyVisualTransformation,
    )
}

enum class TradeType {
    BUY, SELL
}
