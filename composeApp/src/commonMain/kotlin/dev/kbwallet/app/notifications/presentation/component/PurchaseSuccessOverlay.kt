package dev.kbwallet.app.notifications.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sqrt

/** How long the whole celebration stays up before handing control back. */
private const val HoldMillis = 1500L

/**
 * The confirmation shown the moment a purchase goes through: a badge springs
 * in, a ring pulses outward, and the checkmark draws itself stroke by stroke.
 *
 * Before this existed the trade sheet simply closed on success, which was
 * indistinguishable from dismissing it by hand — nothing told the user the
 * money had actually moved.
 *
 * [onFinished] fires once the animation has played out; the caller uses it to
 * close the sheet.
 */
@Composable
fun PurchaseSuccessOverlay(
    title: String,
    subtitle: String,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    val badgeScale = remember { Animatable(0.4f) }
    val ringProgress = remember { Animatable(0f) }
    val checkProgress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Badge first, then the tick draws into it — sequencing them reads as
        // "confirmed", where playing both at once reads as a flash.
        badgeScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        checkProgress.animateTo(1f, tween(durationMillis = 340, easing = FastOutSlowInEasing))
        textAlpha.animateTo(1f, tween(durationMillis = 220))
        delay(HoldMillis)
        onFinished()
    }

    LaunchedEffect(Unit) {
        ringProgress.animateTo(1f, tween(durationMillis = 900, easing = LinearOutSlowInEasing))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Canvas(modifier = Modifier.size(96.dp)) {
                val side = size.minDimension
                val center = Offset(size.width / 2f, size.height / 2f)
                val badgeRadius = side / 2f * badgeScale.value

                // Ring pulse — expands past the badge while fading out.
                val ring = ringProgress.value
                if (ring < 1f) {
                    drawCircle(
                        color = accentColor.copy(alpha = (1f - ring) * 0.45f),
                        radius = badgeRadius * (1f + ring * 0.9f),
                        center = center,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }

                drawCircle(
                    color = accentColor.copy(alpha = 0.18f),
                    radius = badgeRadius,
                    center = center,
                )
                drawCircle(
                    color = accentColor,
                    radius = badgeRadius,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx()),
                )

                drawCheckmark(
                    progress = checkProgress.value,
                    boxSize = side,
                    color = accentColor,
                    strokeWidth = 5.dp.toPx(),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha.value),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha.value),
            )
        }
    }
}

/**
 * Draws the tick as two straight strokes revealed in sequence.
 *
 * Deliberately not a Path + PathMeasure: two interpolated line segments give
 * the same "drawn by hand" reveal with plain arithmetic, and no dependency on
 * PathMeasure's segment behaviour holding across platforms.
 */
private fun DrawScope.drawCheckmark(
    progress: Float,
    boxSize: Float,
    color: Color,
    strokeWidth: Float,
) {
    if (progress <= 0f) return

    val originX = (size.width - boxSize) / 2f
    val originY = (size.height - boxSize) / 2f
    fun point(fx: Float, fy: Float) = Offset(originX + boxSize * fx, originY + boxSize * fy)

    val start = point(0.30f, 0.52f)
    val elbow = point(0.44f, 0.66f)
    val end = point(0.72f, 0.36f)

    val firstLength = distance(start, elbow)
    val secondLength = distance(elbow, end)
    val totalLength = firstLength + secondLength
    if (totalLength <= 0f) return

    val drawn = progress.coerceIn(0f, 1f) * totalLength

    val firstFraction = (drawn / firstLength).coerceIn(0f, 1f)
    drawLine(
        color = color,
        start = start,
        end = lerp(start, elbow, firstFraction),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )

    if (drawn > firstLength) {
        val secondFraction = ((drawn - firstLength) / secondLength).coerceIn(0f, 1f)
        drawLine(
            color = color,
            start = elbow,
            end = lerp(elbow, end, secondFraction),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private fun distance(a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    return sqrt(dx * dx + dy * dy)
}

private fun lerp(a: Offset, b: Offset, fraction: Float) =
    Offset(a.x + (b.x - a.x) * fraction, a.y + (b.y - a.y) * fraction)
