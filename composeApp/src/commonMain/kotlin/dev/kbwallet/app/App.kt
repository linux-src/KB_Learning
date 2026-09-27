package dev.kbwallet.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.kbwallet.app.analytics.presentation.PnLScreen
import dev.kbwallet.app.chart.presentation.CryptoChartScreen
import dev.kbwallet.app.coins.presentation.CoinListScreen
import dev.kbwallet.app.core.auth.presentation.LoginScreen
import dev.kbwallet.app.core.auth.presentation.RegisterScreen
import dev.kbwallet.app.core.biometric.BiometricScreen
import dev.kbwallet.app.core.navigation.ActiveOrders
import dev.kbwallet.app.core.navigation.Biometric
import dev.kbwallet.app.core.navigation.Coins
import dev.kbwallet.app.core.navigation.CryptoChart
import dev.kbwallet.app.core.navigation.Dashboard
import dev.kbwallet.app.core.navigation.EditProfile
import dev.kbwallet.app.core.navigation.HelpSupport
import dev.kbwallet.app.core.navigation.History
import dev.kbwallet.app.core.navigation.LanguageSettings
import dev.kbwallet.app.core.navigation.Login
import dev.kbwallet.app.core.navigation.NotificationCenter
import dev.kbwallet.app.core.navigation.NotificationSettings
import dev.kbwallet.app.core.navigation.PnLAnalytics
import dev.kbwallet.app.core.navigation.Portfolio
import dev.kbwallet.app.core.navigation.Profile
import dev.kbwallet.app.core.navigation.Register
import dev.kbwallet.app.core.navigation.SecuritySettings
import dev.kbwallet.app.core.navigation.Simulator
import dev.kbwallet.app.core.navigation.Sponsorship
import dev.kbwallet.app.core.navigation.Library
import dev.kbwallet.app.core.navigation.Topic
import dev.kbwallet.app.core.i18n.AppStrings
import dev.kbwallet.app.core.i18n.ProvideAppLanguage
import dev.kbwallet.app.core.i18n.appStrings

import dev.kbwallet.app.dashboard.presentation.DashboardScreen
import dev.kbwallet.app.library.presentation.LibraryScreen
import dev.kbwallet.app.library.presentation.LibraryTopicScreen
import dev.kbwallet.app.history.presentation.HistoryScreen
import dev.kbwallet.app.portfolio.presentation.PortfolioScreen
import dev.kbwallet.app.profile.presentation.EditProfileScreen
import dev.kbwallet.app.profile.presentation.HelpSupportScreen
import dev.kbwallet.app.profile.presentation.LanguageSettingsScreen
import dev.kbwallet.app.notifications.presentation.NotificationCenterScreen
import dev.kbwallet.app.notifications.presentation.component.InAppNotificationHost
import dev.kbwallet.app.profile.presentation.NotificationSettingsScreen
import dev.kbwallet.app.profile.presentation.ProfileScreen
import dev.kbwallet.app.profile.presentation.SecuritySettingsScreen
import dev.kbwallet.app.profile.presentation.SponsorshipScreen
import dev.kbwallet.app.theme.KBLearningTheme
import dev.kbwallet.app.simulator.presentation.SimulatorScreen

import org.jetbrains.compose.ui.tooling.preview.Preview

// ── Bottom navigation tab definition ──
private enum class BottomTab(
    val icon: ImageVector,
) {
    Dashboard(Icons.Default.Home),
    Portfolio(Icons.Default.PieChart),
    History(Icons.Default.History),
    Profile(Icons.Default.Person),
}

private fun BottomTab.label(strings: AppStrings): String = when (this) {
    BottomTab.Dashboard -> strings.navDashboard
    BottomTab.Portfolio -> strings.navPortfolio
    BottomTab.History -> strings.navHistory
    BottomTab.Profile -> strings.navProfile
}

@Composable
@Preview
fun App() {
    ProvideAppLanguage {
    val navController: NavHostController = rememberNavController()
    KBLearningTheme {
        // Surface (not just a painted Box/Column background) is what actually
        // provides LocalContentColor to everything below it. Without it, any
        // Text/Icon that doesn't set an explicit color falls back to Compose's
        // hardcoded default (black) — invisible on this app's dark background.
        // Only MainScaffold's own Scaffold did this for the 4 bottom-tab
        // screens; every secondary screen (Simulator, Coins, Buy/Sell, Profile
        // sub-screens, Library, ...) sits directly under NavHost with no such
        // wrapper, hence "dark text on dark background" on those screens.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
        // The in-app banner sits above the NavHost rather than inside any one
        // screen, so a notification raised on the trade sheet still shows after
        // the sheet closes and the user has navigated on.
        Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Biometric,
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Biometric entry ──
            composable<Biometric> {
                BiometricScreen(
                    onSuccess = {
                        navController.navigate(Portfolio) {
                            popUpTo(Biometric) { inclusive = true }
                        }
                    },
                    onCreateAccountClicked = { navController.navigate(Register) },
                    onLoginWithAccountClicked = { navController.navigate(Login) },
                )
            }

            // ── Register / Login (email+password account, no biometrics needed) ──
            composable<Register> {
                RegisterScreen(
                    onSuccess = {
                        navController.navigate(Portfolio) {
                            popUpTo(Biometric) { inclusive = true }
                        }
                    },
                    onSwitchToLogin = {
                        navController.navigate(Login) { popUpTo(Register) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Login> {
                LoginScreen(
                    onSuccess = {
                        navController.navigate(Portfolio) {
                            popUpTo(Biometric) { inclusive = true }
                        }
                    },
                    onSwitchToRegister = {
                        navController.navigate(Register) { popUpTo(Login) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Main scaffold with tabs ──
            composable<Dashboard> {
                MainScaffold(navController = navController)
            }
            composable<Portfolio> {
                MainScaffold(navController = navController)
            }
            composable<History> {
                MainScaffold(navController = navController)
            }
            composable<Profile> {
                MainScaffold(navController = navController)
            }

            // ── Secondary screens (no bottom bar) ──
            composable<Coins> {
                CoinListScreen(
                    onCoinClicked = { coinId ->
                        navController.navigate(CryptoChart(coinId, ""))
                    },
                    onChartRequested = { coinId, coinName ->
                        navController.navigate(CryptoChart(coinId, coinName))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<CryptoChart> { navBackStackEntry ->
                val route = navBackStackEntry.toRoute<CryptoChart>()
                CryptoChartScreen(
                    coinId = route.coinId,
                    coinName = route.coinName,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Profile sub-screens ──
            composable<EditProfile> {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<NotificationSettings> {
                NotificationSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<NotificationCenter> {
                NotificationCenterScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<SecuritySettings> {
                SecuritySettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<HelpSupport> {
                HelpSupportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<LanguageSettings> {
                LanguageSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Sponsorship> {
                SponsorshipScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Trading Simulator additions ──
            composable<PnLAnalytics> {
                PnLScreen(onBack = { navController.popBackStack() })
            }
            composable<ActiveOrders> {
                // Active orders screen (limit orders overview)
                // Navigate back
                navController.popBackStack()
            }
            composable<Simulator> {
                SimulatorScreen(onBack = { navController.popBackStack() })
            }

            // ── Knowledge library ──
            composable<Library> {
                LibraryScreen(
                    onBack = { navController.popBackStack() },
                    onTopicClicked = { topicId -> navController.navigate(Topic(topicId)) },
                )
            }
            composable<Topic> { navBackStackEntry ->
                val topicId: String = navBackStackEntry.toRoute<Topic>().topicId
                LibraryTopicScreen(
                    topicId = topicId,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        InAppNotificationHost(
            modifier = Modifier.align(Alignment.TopCenter),
            onBannerClicked = { navController.navigate(NotificationCenter) },
        )
        }
        }
    }
    }
}

// Switching between these four tabs used to go through a nested NavHost of its
// own (innerNavController), relying on Navigation-Compose's popUpTo/saveState/
// restoreState machinery to avoid piling up a fresh screen + ViewModel on every
// tap. That machinery is still alpha-quality in this KMP fork (2.8.0-alpha09)
// and turned out to be the real cause behind the Dashboard/History freezes —
// they're four sibling tabs with no back-stack semantics between them, so a
// NavHost was never necessary here in the first place. Plain state switching
// keeps exactly one instance of each tab's ViewModel alive for the lifetime of
// this screen (scoped to the same ViewModelStoreOwner Koin already resolves
// against), with none of the navigation-library risk.
@Composable
private fun MainScaffold(navController: NavHostController) {
    val strings = appStrings()
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Dashboard) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label(strings),
                            )
                        },
                        label = { Text(text = tab.label(strings)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        ),
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                BottomTab.Dashboard -> DashboardScreen(
                    onDiscoverCoinsClicked = {
                        navController.navigate(Coins)
                    },
                    onCoinItemClicked = { coinId ->
                        navController.navigate(CryptoChart(coinId, ""))
                    },
                    onSimulatorClicked = {
                        navController.navigate(Simulator)
                    },
                    onLibraryClicked = {
                        navController.navigate(Library)
                    },
                )
                BottomTab.Portfolio -> PortfolioScreen(
                    onCoinItemClicked = { coinId ->
                        navController.navigate(CryptoChart(coinId, ""))
                    },
                    onDiscoverCoinsClicked = {
                        navController.navigate(Coins)
                    },
                )
                BottomTab.History -> HistoryScreen()
                BottomTab.Profile -> ProfileScreen(
                    onNavigateToEditProfile = {
                        navController.navigate(EditProfile)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(NotificationSettings)
                    },
                    onNavigateToNotificationCenter = {
                        navController.navigate(NotificationCenter)
                    },
                    onNavigateToSecurity = {
                        navController.navigate(SecuritySettings)
                    },
                    onNavigateToHelp = {
                        navController.navigate(HelpSupport)
                    },
                    onNavigateToPnL = {
                        navController.navigate(PnLAnalytics)
                    },
                    onNavigateToLanguage = {
                        navController.navigate(LanguageSettings)
                    },
                    onNavigateToSponsorship = {
                        navController.navigate(Sponsorship)
                    },
                )
            }
        }
    }
}
