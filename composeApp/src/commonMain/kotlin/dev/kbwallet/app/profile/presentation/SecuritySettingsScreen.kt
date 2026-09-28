package dev.kbwallet.app.profile.presentation

import androidx.compose.foundation.layout.PaddingValues
import dev.kbwallet.app.theme.component.KBCard
import dev.kbwallet.app.theme.component.MenuRow
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.component.SettingsSectionLabel
import dev.kbwallet.app.theme.component.ToggleCard
import dev.kbwallet.app.theme.component.BackHeader
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.AppStrings
import dev.kbwallet.app.core.i18n.appStrings
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showPasswordDialog by remember { mutableStateOf(false) }
    val strings = appStrings()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackHeader(title = strings.securityTitle, onBack = onNavigateBack)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.xs),
            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            // ── AUTHENTICATION ──
            item {
                SectionHeader(strings.sectionAuthentication)
            }
            item {
                ToggleItem(
                    icon = Icons.Default.Fingerprint,
                    title = strings.securityBiometricTitle,
                    subtitle = strings.securityBiometricSubtitle,
                    checked = state.biometricAuth,
                    onToggle = { viewModel.toggleBiometricAuth() },
                )
            }
            item {
                ToggleItem(
                    icon = Icons.Default.PhonelinkLock,
                    title = strings.security2faTitle,
                    subtitle = strings.security2faSubtitle,
                    checked = state.twoFactorAuth,
                    onToggle = { viewModel.toggleTwoFactorAuth() },
                )
            }

            // ── ACCOUNT ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(strings.sectionAccount)
            }
            item {
                KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                    MenuRow(
                        icon = Icons.Default.Lock,
                        title = strings.securityChangePassword,
                        onClick = { showPasswordDialog = true },
                    )
                }
            }
        }
    }

    // ── Password Change Dialog ──
    if (showPasswordDialog) {
        ChangePasswordDialog(
            strings = strings,
            onDismiss = { showPasswordDialog = false },
            onConfirm = { /* Handle password change */ showPasswordDialog = false },
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val isFormValid = currentPassword.isNotBlank()
            && newPassword.isNotBlank()
            && confirmPassword.isNotBlank()
            && newPassword == confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.securityChangePassword,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text(strings.dialogCurrentPasswordLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(strings.dialogNewPasswordLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(strings.dialogConfirmPasswordLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(strings.actionConfirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = strings.actionCancel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun SectionHeader(title: String) = SettingsSectionLabel(title)

@Composable
private fun ToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit,
) = ToggleCard(icon = icon, title = title, subtitle = subtitle, checked = checked, onToggle = onToggle)
