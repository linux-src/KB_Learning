package dev.kbwallet.app.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kblearning.composeapp.generated.resources.Res
import kblearning.composeapp.generated.resources.manrope_bold
import kblearning.composeapp.generated.resources.manrope_extrabold
import kblearning.composeapp.generated.resources.manrope_medium
import kblearning.composeapp.generated.resources.manrope_regular
import kblearning.composeapp.generated.resources.manrope_semibold
import org.jetbrains.compose.resources.Font

@Composable
internal fun manropeFamily(): FontFamily = FontFamily(
    Font(Res.font.manrope_regular, FontWeight.Normal),
    Font(Res.font.manrope_medium, FontWeight.Medium),
    Font(Res.font.manrope_semibold, FontWeight.SemiBold),
    Font(Res.font.manrope_bold, FontWeight.Bold),
    Font(Res.font.manrope_extrabold, FontWeight.ExtraBold),
)

/** Tabular figures, so changing numbers don't jitter. */
fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")

@Composable
internal fun kbTypography(): Typography {
    val family = manropeFamily()
    val base = Typography()
    fun TextStyle.withFamily(weight: FontWeight, tracking: Float = 0f) =
        copy(fontFamily = family, fontWeight = weight, letterSpacing = tracking.em)

    return Typography(
        displayLarge = base.displayLarge.withFamily(FontWeight.ExtraBold, -0.02f),
        displayMedium = base.displayMedium.withFamily(FontWeight.ExtraBold, -0.02f),
        displaySmall = base.displaySmall.withFamily(FontWeight.Bold, -0.02f),
        headlineLarge = base.headlineLarge.withFamily(FontWeight.ExtraBold, -0.015f),
        headlineMedium = base.headlineMedium.copy(fontSize = 26.sp, lineHeight = 32.sp)
            .withFamily(FontWeight.ExtraBold, -0.015f),
        headlineSmall = base.headlineSmall.withFamily(FontWeight.Bold, -0.01f),
        titleLarge = base.titleLarge.copy(fontSize = 19.sp, lineHeight = 26.sp)
            .withFamily(FontWeight.Bold, -0.005f),
        titleMedium = base.titleMedium.withFamily(FontWeight.Bold),
        titleSmall = base.titleSmall.withFamily(FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.withFamily(FontWeight.Normal),
        bodyMedium = base.bodyMedium.withFamily(FontWeight.Normal),
        bodySmall = base.bodySmall.withFamily(FontWeight.Medium),
        labelLarge = base.labelLarge.withFamily(FontWeight.Bold, 0.005f),
        labelMedium = base.labelMedium.withFamily(FontWeight.SemiBold, 0.01f),
        labelSmall = base.labelSmall.withFamily(FontWeight.SemiBold, 0.02f),
    )
}
