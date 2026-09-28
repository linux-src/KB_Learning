package dev.kbwallet.app.profile.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import dev.kbwallet.app.core.util.AppInfo
import dev.kbwallet.app.theme.component.AppMark
import dev.kbwallet.app.theme.component.IconBadge
import dev.kbwallet.app.theme.component.Tag
import dev.kbwallet.app.theme.mode.ThemeController
import dev.kbwallet.app.theme.mode.ThemeMode
import org.koin.compose.koinInject
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.component.KBCard
import dev.kbwallet.app.theme.component.MenuRow
import dev.kbwallet.app.theme.component.RowDivider
import dev.kbwallet.app.theme.component.ScreenTitle
import dev.kbwallet.app.theme.component.StatCard
import dev.kbwallet.app.theme.component.screenContentPadding
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
    val themeController = koinInject<ThemeController>()
    val themeMode by themeController.mode.collectAsStateWithLifecycle()
    val strings = appStrings()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        item { ScreenTitle(title = strings.profileTitle) }

        // ── Identity card ──
        item {
            KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Dimens.lg)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.avatarInitial,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Spacer(Modifier.width(Dimens.md))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = state.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = state.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    FilledTonalIconButton(onClick = onNavigateToEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = strings.profileEditButton)
                    }
                }
            }
        }

        // ── Stats ──
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
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
        item { MenuGroupLabel(strings.sectionGeneral) }
        item {
            MenuGroup {
                MenuRow(Icons.Default.Person, strings.profileMenuPersonalInfoTitle, onNavigateToEditProfile, subtitle = strings.profileMenuPersonalInfoSubtitle)
                RowDivider()
                MenuRow(Icons.Default.Lock, strings.profileMenuSecurityTitle, onNavigateToSecurity, subtitle = strings.profileMenuSecuritySubtitle)
                RowDivider()
                MenuRow(Icons.Default.Language, strings.profileMenuLanguageTitle, onNavigateToLanguage, subtitle = strings.profileMenuLanguageSubtitle)
                RowDivider()
                ThemeRow(selected = themeMode, onSelect = themeController::setMode)
            }
        }
        item { MenuGroupLabel(strings.sectionTrading) }
        item {
            MenuGroup {
                MenuRow(Icons.AutoMirrored.Filled.TrendingUp, strings.profileMenuPnlTitle, onNavigateToPnL, subtitle = strings.profileMenuPnlSubtitle)
                RowDivider()
                MenuRow(Icons.Default.Notifications, strings.profileMenuNotificationCenterTitle, onNavigateToNotificationCenter, subtitle = strings.profileMenuNotificationCenterSubtitle)
                RowDivider()
                MenuRow(Icons.Default.Tune, strings.profileMenuNotificationsTitle, onNavigateToNotifications, subtitle = strings.profileMenuNotificationsSubtitle)
            }
        }
        item { MenuGroupLabel(strings.sectionOther) }
        item {
            MenuGroup {
                MenuRow(Icons.AutoMirrored.Filled.HelpOutline, strings.profileMenuHelpTitle, onNavigateToHelp, subtitle = strings.profileMenuHelpSubtitle)
                RowDivider()
                MenuRow(
                    Icons.Default.Favorite,
                    strings.profileMenuSponsorshipTitle,
                    onNavigateToSponsorship,
                    subtitle = strings.profileMenuSponsorshipSubtitle,
                    tint = KBTheme.colors.lossRed,
                )
            }
        }
        item { AboutCard() }
    }
}

@Composable
private fun ThemeRow(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val strings = appStrings()
    Column(Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.Palette)
            Spacer(Modifier.width(Dimens.md))
            Text(strings.themeTitle, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(Dimens.sm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            listOf(
                Triple(ThemeMode.SYSTEM, Icons.Default.BrightnessAuto, strings.themeSystem),
                Triple(ThemeMode.LIGHT, Icons.Default.LightMode, strings.themeLight),
                Triple(ThemeMode.DARK, Icons.Default.DarkMode, strings.themeDark),
            ).forEach { (mode, icon, label) ->
                val isSelected = mode == selected
                val bg by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    label = "themeSegment",
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(bg)
                        .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(mode) },
                ) {
                    val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(label, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun AboutCard() {
    val strings = appStrings()
    val uriHandler = LocalUriHandler.current
    Column {
        MenuGroupLabel(strings.aboutTitle)
        Spacer(Modifier.height(Dimens.md))
        KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Dimens.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppMark(size = 48.dp)
                Spacer(Modifier.width(Dimens.md))
                Column(Modifier.weight(1f)) {
                    Text(strings.appName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        strings.aboutVersion(AppInfo.VERSION),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Tag(strings.aboutLicense, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.height(Dimens.md))
            Text(
                strings.aboutDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    strings.aboutStack,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { uriHandler.openUri(AppInfo.SOURCE_URL) }) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.aboutSourceCode)
                }
            }
        }
    }
}

@Composable
private fun MenuGroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Dimens.xxs, top = Dimens.xs),
    )
}

@Composable
private fun MenuGroup(content: @Composable () -> Unit) {
    KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        content()
    }
}
