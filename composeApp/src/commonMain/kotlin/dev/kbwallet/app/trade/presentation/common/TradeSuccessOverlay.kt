package dev.kbwallet.app.trade.presentation.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.tabular
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val AutoCloseMillis = 2800
private const val ParticleCount = 18

data class TradeResult(
    val isSell: Boolean,
    val coinAmount: String,
    val price: Double,
    val total: Double,
)

/** Buy/sell confirmation. Closes after [AutoCloseMillis] or on "Done". */
@Composable
fun TradeSuccessOverlay(
    result: TradeResult,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = appStrings()
    val haptics = LocalHapticFeedback.current
    val finish by rememberUpdatedState(onFinished)
    val accent = if (result.isSell) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val onAccent = if (result.isSell) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary

    val backdrop = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0.3f) }
    val burst = remember { Animatable(0f) }
    val check = remember { Animatable(0f) }
    val counter = remember { Animatable(0f) }
    val content = remember { Animatable(0f) }
    val countdown = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { backdrop.animateTo(1f, tween(180)) }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        launch { burst.animateTo(1f, tween(900, easing = LinearOutSlowInEasing)) }
        badgeScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
        launch { check.animateTo(1f, tween(320, easing = FastOutSlowInEasing)) }
        launch { counter.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        content.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
        countdown.animateTo(1f, tween(AutoCloseMillis, easing = LinearEasing))
        delay(120)
        finish()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = backdrop.value }
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 420.dp).padding(Dimens.xl),
        ) {
            Canvas(modifier = Modifier.size(132.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val badgeRadius = size.minDimension * 0.3f * badgeScale.value

                drawBurst(center, size.minDimension / 2f, burst.value, accent)

                drawCircle(accent.copy(alpha = 0.16f), radius = badgeRadius * 1.25f, center = center)
                drawCircle(accent, radius = badgeRadius, center = center)
                drawCheckmark(check.value, center, badgeRadius, onAccent, 4.5.dp.toPx())
            }

            Spacer(Modifier.height(Dimens.xs))
            Text(
                text = if (result.isSell) strings.tradeSellSuccessTitle else strings.notifPurchaseTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = content.value },
            )
            Spacer(Modifier.height(Dimens.xxs))
            Text(
                text = (if (result.isSell) "+" else "−") + formatFiat(result.total * counter.value),
                style = MaterialTheme.typography.displaySmall.tabular(),
                color = accent,
            )
            Spacer(Modifier.height(Dimens.xxs))
            Text(
                text = if (result.isSell) strings.tradeSellSuccessSubtitle(result.coinAmount)
                else strings.tradeSuccessSubtitle(result.coinAmount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = content.value },
            )

            Spacer(Modifier.height(Dimens.lg))
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.xs),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = content.value
                        translationY = (1f - content.value) * 24.dp.toPx()
                    }
                    .clip(RoundedCornerShape(Dimens.tileRadius))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(Dimens.md),
            ) {
                SummaryRow(strings.tradeSummaryPrice, formatFiat(result.price))
                SummaryRow(
                    if (result.isSell) strings.tradeSummaryReceived else strings.tradeSummaryPaid,
                    formatFiat(result.total),
                )
            }

            Spacer(Modifier.height(Dimens.lg))
            Button(
                onClick = finish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .graphicsLayer { alpha = content.value },
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            ) {
                Text(strings.actionDone, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(Dimens.sm))
            LinearProgressIndicator(
                progress = { 1f - countdown.value },
                modifier = Modifier.fillMaxWidth(0.3f).height(3.dp).clip(RoundedCornerShape(2.dp)),
                color = accent.copy(alpha = 0.6f),
                trackColor = Color.Transparent,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(value, style = MaterialTheme.typography.titleSmall.tabular(), color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Dots flying outward from the badge, fading as they go. */
private fun DrawScope.drawBurst(center: Offset, maxRadius: Float, progress: Float, color: Color) {
    if (progress <= 0f || progress >= 1f) return
    val inner = maxRadius * 0.45f
    for (i in 0 until ParticleCount) {
        val reach = if (i % 2 == 0) 1f else 0.78f
        val angle = (i.toFloat() / ParticleCount) * 2f * PI.toFloat() + 0.2f
        val distance = inner + (maxRadius * reach - inner) * progress
        val point = Offset(center.x + cos(angle) * distance, center.y + sin(angle) * distance)
        drawCircle(
            color = color.copy(alpha = (1f - progress) * 0.9f),
            radius = (if (i % 3 == 0) 4.5f else 3f) * density * (1f - progress * 0.5f),
            center = point,
        )
    }
}

private fun DrawScope.drawCheckmark(progress: Float, center: Offset, radius: Float, color: Color, strokeWidth: Float) {
    if (progress <= 0f) return
    fun point(fx: Float, fy: Float) = Offset(center.x + radius * fx, center.y + radius * fy)
    val start = point(-0.42f, 0.02f)
    val elbow = point(-0.12f, 0.32f)
    val end = point(0.45f, -0.3f)

    val first = distance(start, elbow)
    val second = distance(elbow, end)
    val drawn = progress.coerceIn(0f, 1f) * (first + second)

    drawLine(color, start, lerp(start, elbow, (drawn / first).coerceIn(0f, 1f)), strokeWidth, StrokeCap.Round)
    if (drawn > first) {
        drawLine(color, elbow, lerp(elbow, end, ((drawn - first) / second).coerceIn(0f, 1f)), strokeWidth, StrokeCap.Round)
    }
}

private fun distance(a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    return sqrt(dx * dx + dy * dy)
}

private fun lerp(a: Offset, b: Offset, fraction: Float) =
    Offset(a.x + (b.x - a.x) * fraction, a.y + (b.y - a.y) * fraction)
