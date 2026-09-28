package dev.kbwallet.app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Spacing, radii and layout limits shared by all screens. */
object Dimens {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Horizontal page gutter. */
    val screenPadding = 20.dp

    /** Vertical gap between sections of a scrolling screen. */
    val sectionGap = 24.dp

    /** Gap between sibling cards inside a section. */
    val itemGap = 10.dp

    /** Max width of the content column on wide windows. */
    val contentMaxWidth = 720.dp

    /** Above this window width the bottom bar becomes a side rail. */
    val railBreakpoint = 840.dp

    val cardRadius = 20.dp
    val tileRadius = 16.dp
    val controlRadius = 14.dp
    val chipRadius = 10.dp

    /** Minimum comfortable touch target. */
    val minTouch = 48.dp
}

internal val KBShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(Dimens.chipRadius),
    medium = RoundedCornerShape(Dimens.controlRadius),
    large = RoundedCornerShape(Dimens.cardRadius),
    extraLarge = RoundedCornerShape(28.dp),
)
