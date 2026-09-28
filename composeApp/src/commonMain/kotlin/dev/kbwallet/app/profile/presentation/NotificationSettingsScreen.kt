package dev.kbwallet.app.profile.presentation

import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.component.SettingsSectionLabel
import dev.kbwallet.app.theme.component.ToggleCard
import dev.kbwallet.app.theme.component.BackHeader
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.notifications.domain.NotificationController
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val notificationController = koinInject<NotificationController>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    // The switch below is the user's intent; whether the OS will actually
    // deliver is a separate thing it can't see. Re-read on entry, since the
    // user may have just changed it in system settings.
    var systemPermitted by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        systemPermitted = notificationController.isSystemNotificationPermitted()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackHeader(title = strings.notificationsTitle, onBack = onNavigateBack)
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
            // ── GENERAL ──
            item {
                SectionHeader(strings.sectionGeneral)
            }
            item {
                ToggleItem(
                    icon = Icons.Default.Notifications,
                    title = strings.notifPushTitle,
                    subtitle = strings.notifPushSubtitle,
                    checked = state.pushNotifications,
                    onToggle = {
                        // Turning it on is the one clear moment of intent to
                        // point at the OS permission screen.
                        if (!state.pushNotifications && !systemPermitted) {
                            notificationController.requestSystemPermission()
                        }
                        viewModel.togglePushNotifications()
                    },
                )
            }
            if (state.pushNotifications && !systemPermitted) {
                item {
                    SystemBlockedRow(
                        message = strings.notifSystemBlockedTitle,
                        actionLabel = strings.notifSystemBlockedAction,
                        onAction = { notificationController.requestSystemPermission() },
                    )
                }
            }
            item {
                ToggleItem(
                    icon = Icons.Default.Email,
                    title = strings.notifEmailTitle,
                    subtitle = strings.notifEmailSubtitle,
                    checked = state.emailNotifications,
                    onToggle = { viewModel.toggleEmailNotifications() },
                )
            }

            // ── TRADING ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(strings.sectionTrading)
            }
            item {
                ToggleItem(
                    icon = Icons.Default.TrendingUp,
                    title = strings.notifPriceAlertsTitle,
                    subtitle = strings.notifPriceAlertsSubtitle,
                    checked = state.priceAlerts,
                    onToggle = { viewModel.togglePriceAlerts() },
                )
            }
            item {
                ToggleItem(
                    icon = Icons.Default.Receipt,
                    title = strings.notifTradeConfirmationsTitle,
                    subtitle = strings.notifTradeConfirmationsSubtitle,
                    checked = state.tradeConfirmations,
                    onToggle = { viewModel.toggleTradeConfirmations() },
                )
            }

            // ── OTHER ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(strings.sectionOther)
            }
            item {
                ToggleItem(
                    icon = Icons.Default.Newspaper,
                    title = strings.notifNewsTitle,
                    subtitle = strings.notifNewsSubtitle,
                    checked = state.newsUpdates,
                    onToggle = { viewModel.toggleNewsUpdates() },
                )
            }
        }
    }
}

/**
 * Shown when the user's push switch is on but the OS is dropping everything —
 * without it the screen looks correctly configured while nothing is delivered.
 */
@Composable
private fun SystemBlockedRow(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                LocalKBLearningColorsPalette.current.lossRed.copy(alpha = 0.12f),
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = null,
            tint = LocalKBLearningColorsPalette.current.lossRed,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAction) {
            Text(actionLabel, color = MaterialTheme.colorScheme.primary)
        }
    }
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
