package dev.kbwallet.app.core.biometric

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kbwallet.app.core.biometric.BiometricAuthNotAvailable
import dev.kbwallet.app.core.biometric.getBiometricAuthenticator
import dev.kbwallet.app.core.biometric.getPlatformContext
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
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

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "KB Learning",
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.biometricTagline,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(64.dp))
            // TODO Icon
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
                }
            ) {
                Text(
                    text = strings.biometricLoginButton
                )
            }
            authError?.let {
                Text(
                    text = it,
                    color = LocalKBLearningColorsPalette.current.lossRed,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Biometric-only login has no account behind it at all — these give
            // anyone without (or who doesn't want to use) biometrics a way in.
            TextButton(onClick = onCreateAccountClicked) {
                Text(strings.biometricCreateAccountPrompt, color = MaterialTheme.colorScheme.primary)
            }
            TextButton(onClick = onLoginWithAccountClicked) {
                Text(strings.biometricLoginWithAccountPrompt, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Text(
            text = strings.biometricDisclaimer,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        )
    }
}