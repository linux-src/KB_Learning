package dev.kbwallet.app.core.biometric

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.kbwallet.app.core.biometric.BiometricAuthNotAvailable
import dev.kbwallet.app.core.biometric.getBiometricAuthenticator
import dev.kbwallet.app.core.biometric.getPlatformContext
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.component.AppMark
import dev.kbwallet.app.theme.component.IconBadge
import dev.kbwallet.app.theme.component.KBCard
import kotlinx.coroutines.launch

@Composable
fun BiometricScreen(
    onSuccess: () -> Unit,
    onCreateAccountClicked: () -> Unit = {},
    onLoginWithAccountClicked: () -> Unit = {},
) {
    val platformContext = getPlatformContext()
    val biometricAuthenticator = remember { getBiometricAuthenticator(platformContext) }
    val coroutineScope = rememberCoroutineScope()
    var authError by remember { mutableStateOf<String?>(null) }
    val strings = appStrings()

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val alpha by animateFloatAsState(if (appeared) 1f else 0f, tween(500), label = "introAlpha")
    val offset by animateDpAsState(if (appeared) 0.dp else 16.dp, tween(500), label = "introOffset")

    val glow = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow, Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.18f),
                        radius = size.minDimension * 0.75f,
                    ),
                    radius = size.minDimension * 0.75f,
                    center = Offset(size.width / 2f, size.height * 0.18f),
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.xl, vertical = Dimens.xxl)
                .graphicsLayer { this.alpha = alpha; translationY = offset.toPx() },
        ) {
            AppMark(size = 72.dp)
            Spacer(modifier = Modifier.height(Dimens.lg))
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(Dimens.xs))
            Text(
                text = strings.biometricTagline,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Dimens.xxl))
            KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Dimens.md)) {
                FeatureRow(Icons.Default.PieChart, strings.biometricFeatureTrack)
                Spacer(Modifier.height(Dimens.sm))
                FeatureRow(Icons.AutoMirrored.Filled.ShowChart, strings.biometricFeaturePractice)
                Spacer(Modifier.height(Dimens.sm))
                FeatureRow(Icons.AutoMirrored.Filled.MenuBook, strings.biometricFeatureLearn)
            }

            Spacer(modifier = Modifier.height(Dimens.xxl))
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val authenticated = biometricAuthenticator.authenticate()
                            authError = null
                            if (authenticated) {
                                onSuccess()
                            }
                        } catch (e: Exception) {
                            authError = e.message
                            if (e.message == BiometricAuthNotAvailable.BIOAUTH_NOT_AVAILABLE.toString()) {
                                authError = strings.biometricNotAvailable
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(Dimens.xs))
                Text(text = strings.biometricLoginButton, style = MaterialTheme.typography.titleSmall)
            }
            AnimatedVisibility(visible = authError != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = Dimens.sm)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.controlRadius))
                        .background(KBTheme.colors.lossContainer)
                        .padding(Dimens.sm),
                ) {
                    Icon(Icons.Default.ErrorOutline, null, tint = KBTheme.colors.lossRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Dimens.xs))
                    Text(
                        text = authError.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.sm))
            // Biometric-only login has no account behind it at all — these give
            // anyone without (or who doesn't want to use) biometrics a way in.
            OutlinedButton(
                onClick = onCreateAccountClicked,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                Text(strings.biometricCreateAccountPrompt, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(Dimens.xs))
            TextButton(onClick = onLoginWithAccountClicked, modifier = Modifier.heightIn(min = Dimens.minTouch)) {
                Text(strings.biometricLoginWithAccountPrompt, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(Dimens.xl))
            Text(
                text = strings.biometricDisclaimer,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon = icon, size = 36.dp, iconSize = 18.dp)
        Spacer(Modifier.width(Dimens.sm))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
