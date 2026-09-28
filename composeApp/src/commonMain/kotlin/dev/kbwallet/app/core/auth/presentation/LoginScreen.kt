package dev.kbwallet.app.core.auth.presentation

import dev.kbwallet.app.theme.component.AppMark
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onSuccess: () -> Unit,
    onSwitchToRegister: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is LoginEvent.Success) onSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppMark(size = 56.dp)
            Spacer(Modifier.height(20.dp))
            Text(
                text = strings.authLoginTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text(strings.authEmailLabel) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors(),
            )
            Spacer(Modifier.height(12.dp))

            PasswordField(
                value = state.password,
                onValueChange = viewModel::onPasswordChanged,
                label = strings.authPasswordLabel,
            )

            state.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error.label(strings),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LocalKBLearningColorsPalette.current.lossContainer)
                        .padding(12.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = viewModel::onLoginClicked,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(strings.authLoginButton)
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onSwitchToRegister) {
                Text(strings.authSwitchToRegisterPrompt, color = MaterialTheme.colorScheme.primary)
            }
            TextButton(onClick = onBack) {
                Text(strings.authBackToBiometricPrompt, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
