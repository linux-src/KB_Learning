package dev.kbwallet.app.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.tabular

/** Gradient total balance card used on Dashboard and Portfolio. */
@Composable
fun BalanceHeroCard(
    label: String,
    value: String,
    figures: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    change: String? = null,
    changePositive: Boolean = true,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    val shape = RoundedCornerShape(Dimens.cardRadius + 4.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(KBTheme.colors.heroGradient))
            .padding(Dimens.xl),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.72f),
        )
        Spacer(Modifier.height(Dimens.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.tabular(),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (change != null) {
                Spacer(Modifier.width(Dimens.sm))
                ChangePill(text = change, isPositive = changePositive)
            }
        }
        if (figures.isNotEmpty()) {
            Spacer(Modifier.height(Dimens.lg))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.tileRadius))
                    .background(Color.Black.copy(alpha = 0.18f))
                    .padding(horizontal = Dimens.md, vertical = Dimens.sm),
            ) {
                figures.forEach { (figureLabel, figureValue) ->
                    Column(Modifier.weight(1f)) {
                        Text(
                            figureLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            figureValue,
                            style = MaterialTheme.typography.titleMedium.tabular(),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        footer()
    }
}
