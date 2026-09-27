package dev.kbwallet.app.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.component.StatCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToNotificationCenter: () -> Unit = {},
    onNavigateToSecurity: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToSponsorship: () -> Unit,
    onNavigateToPnL: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ──
        item {
            Text(
                text = strings.profileTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // ── Avatar + Info ──
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.avatarInitial,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = state.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onNavigateToEditProfile,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                ) {
                    Text(
                        text = strings.profileEditButton,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // ── Stats Row ──
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
            ) {
                StatCard(
                    title = strings.profileStatTotalTrades,
                    value = state.totalTrades.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = strings.profileStatWinRate,
                    value = state.winRate,
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary,
                )
                StatCard(
                    title = strings.profileStatDaysActive,
                    value = state.daysActive,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Menu ──
        item {
            ProfileMenuOption(
                icon = Icons.Default.Person,
                title = strings.profileMenuPersonalInfoTitle,
                subtitle = strings.profileMenuPersonalInfoSubtitle,
                onClick = onNavigateToEditProfile,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.Default.Notifications,
                title = strings.profileMenuNotificationCenterTitle,
                subtitle = strings.profileMenuNotificationCenterSubtitle,
                onClick = onNavigateToNotificationCenter,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.Default.Tune,
                title = strings.profileMenuNotificationsTitle,
                subtitle = strings.profileMenuNotificationsSubtitle,
                onClick = onNavigateToNotifications,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.Default.Lock,
                title = strings.profileMenuSecurityTitle,
                subtitle = strings.profileMenuSecuritySubtitle,
                onClick = onNavigateToSecurity,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.Default.TrendingUp,
                title = strings.profileMenuPnlTitle,
                subtitle = strings.profileMenuPnlSubtitle,
                onClick = onNavigateToPnL,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.Default.Language,
                title = strings.profileMenuLanguageTitle,
                subtitle = strings.profileMenuLanguageSubtitle,
                onClick = onNavigateToLanguage,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.Default.Favorite,
                title = strings.profileMenuSponsorshipTitle,
                subtitle = strings.profileMenuSponsorshipSubtitle,
                onClick = onNavigateToSponsorship,
            )
        }
        item {
            ProfileMenuOption(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = strings.profileMenuHelpTitle,
                subtitle = strings.profileMenuHelpSubtitle,
                onClick = onNavigateToHelp,
            )
        }
    }
}

@Composable
private fun ProfileMenuOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
    }
}
