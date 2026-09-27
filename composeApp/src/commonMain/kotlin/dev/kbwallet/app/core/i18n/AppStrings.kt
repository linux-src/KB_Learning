package dev.kbwallet.app.core.i18n

import androidx.compose.runtime.Composable

/**
 * All user-facing UI copy the app switches between English/Russian at
 * runtime via [LocalAppLanguage] — kept as one flat catalog (as opposed to
 * Compose Multiplatform's composeResources/values-ru, which only follows
 * the OS locale and can't be overridden reliably across platforms, see
 * [[AppLanguage]]).
 */
interface AppStrings {
    // ── Shared ──
    val actionBack: String
    val actionConfirm: String
    val actionCancel: String
    val actionRetry: String

    // ── Profile screen ──
    val profileTitle: String
    val profileEditButton: String
    val profileStatTotalTrades: String
    val profileStatWinRate: String
    val profileStatDaysActive: String
    val profileMenuPersonalInfoTitle: String
    val profileMenuPersonalInfoSubtitle: String
    val profileMenuNotificationsTitle: String
    val profileMenuNotificationsSubtitle: String
    val profileMenuSecurityTitle: String
    val profileMenuSecuritySubtitle: String
    val profileMenuPnlTitle: String
    val profileMenuPnlSubtitle: String
    val profileMenuSponsorshipTitle: String
    val profileMenuSponsorshipSubtitle: String
    val sponsorshipTitle: String
    val sponsorshipDesc: String
    val sponsorshipCoffee: String
    val sponsorshipCoffeeSub: String
    val sponsorshipBeer: String
    val sponsorshipBeerSub: String
    val profileMenuHelpTitle: String
    val profileMenuHelpSubtitle: String
    val profileMenuLanguageTitle: String
    val profileMenuLanguageSubtitle: String

    // ── Edit profile screen ──
    val editProfileTitle: String
    val editProfileDisplayNameLabel: String
    val editProfileEmailLabel: String
    val editProfileSaveButton: String
    val editProfileUpdateSuccess: String

    // ── Notification settings screen ──
    val notificationsTitle: String
    val sectionGeneral: String
    val notifPushTitle: String
    val notifPushSubtitle: String
    val notifEmailTitle: String
    val notifEmailSubtitle: String
    val sectionTrading: String
    val notifPriceAlertsTitle: String
    val notifPriceAlertsSubtitle: String
    val notifTradeConfirmationsTitle: String
    val notifTradeConfirmationsSubtitle: String
    val sectionOther: String
    val notifNewsTitle: String
    val notifNewsSubtitle: String

    // ── Notification centre & trade feedback ──
    val profileMenuNotificationCenterTitle: String
    val profileMenuNotificationCenterSubtitle: String
    val notifCenterEmptyTitle: String
    val notifCenterEmptySubtitle: String
    val notifCenterMarkAllRead: String
    val notifCenterClearAll: String
    val notifSystemBlockedTitle: String
    val notifSystemBlockedAction: String
    val notifPurchaseTitle: String
    fun notifPurchaseBody(amount: String, total: String): String
    fun tradeSuccessSubtitle(amount: String): String

    // ── Security settings screen ──
    val securityTitle: String
    val sectionAuthentication: String
    val securityBiometricTitle: String
    val securityBiometricSubtitle: String
    val security2faTitle: String
    val security2faSubtitle: String
    val sectionAccount: String
    val securityChangePassword: String
    val dialogCurrentPasswordLabel: String
    val dialogNewPasswordLabel: String
    val dialogConfirmPasswordLabel: String

    // ── Help & support screen ──
    val helpTitle: String
    val helpFaqHeading: String
    val faqQ1: String
    val faqA1: String
    val faqQ2: String
    val faqA2: String
    val faqQ3: String
    val faqA3: String
    val faqQ4: String
    val faqA4: String
    val faqQ5: String
    val faqA5: String
    val helpContactHeading: String
    val helpContactLiveChat: String
    val helpAboutHeading: String
    val helpAboutVersion: String
    val helpAboutTagline: String
    val contentDescCollapse: String
    val contentDescExpand: String

    // ── Language settings screen ──
    val languageTitle: String
    val languageEnglish: String
    val languageRussian: String
    val languageSystemDefault: String

    // ── Dashboard screen ──
    val dashboardTitle: String
    val dashboardSimulatorButton: String
    val dashboardStatTotalBalance: String
    val dashboardStatCash: String
    val dashboardStatPortfolioValue: String
    val dashboardStatAssets: String
    val dashboardStat24hChange: String
    val dashboardMarketOverview: String
    val dashboardTradingTipTitle: String
    val dashboardTradingTipBody: String
    val dashboardLibraryTitle: String
    val dashboardLibrarySubtitle: String
    val dashboardPortfolioSummary: String
    val dashboardNoAssetsTitle: String
    val dashboardNoAssetsSubtitle: String
    fun dashboardAssetsCount(count: Int): String

    // ── Portfolio screen ──
    val portfolioTitle: String
    val portfolioBalanceLabel: String
    val portfolioCashLabel: String
    val portfolioHoldingsLabel: String
    val portfolioDiscoverCoinsButton: String
    val portfolioSearchPlaceholder: String
    val portfolioDistributionTitle: String
    val portfolioNothingToShow: String
    val portfolioYourAssets: String
    fun portfolioCoinsCount(count: Int): String
    val portfolioNoSearchResults: String
    val portfolioEmptyTitle: String
    val portfolioEmptySubtitle: String

    // ── History screen ──
    val historyTitle: String
    val historyStatTotalTrades: String
    val historyStatTotalBuy: String
    val historyStatTotalSell: String
    val historyRecentTransactions: String
    val historyEmptyTitle: String
    val historyEmptySubtitle: String
    val historyBuyLabel: String
    val historySellLabel: String
    fun historyPriceLabel(price: String): String

    // ── Watchlist screen ──
    val watchlistTitle: String
    val watchlistEmptyTitle: String
    val watchlistEmptySubtitle: String

    // ── Coins list screen ──
    val coinsListTitle: String

    // ── Chart screen ──
    val chartModeCandles: String
    val chartModeLine: String

    // ── P&L analytics screen ──
    val pnlTitle: String
    val pnlStatTotalTrades: String
    val pnlStatWinRate: String
    val pnlStatActiveOrders: String
    val pnlRealized: String
    val pnlDetailedStats: String
    val pnlRowTotalInvested: String
    val pnlRowTotalRealized: String
    val pnlRowBestTrade: String
    val pnlRowWorstTrade: String
    val pnlRowAvgProfitPerTrade: String
    val pnlRowBuyOrders: String
    val pnlRowSellOrders: String

    // ── Trade (buy/sell) screen ──
    val tradeCoinFallback: String
    val tradeCoinAmountLabel: String
    val tradeBuyAmountLabel: String
    val tradeSellAmountLabel: String
    val tradeBuyButton: String
    val tradeSellButton: String
    val tradeAvailablePrefix: String

    // ── Trading simulator screen ──
    val simulatorTitle: String
    val simulatorHintContentDesc: String
    val simulatorSelectCoinPrompt: String
    val simulatorErrorFailedToLoadCoins: String
    val simulatorErrorNotEnoughData: String
    val simulatorErrorFailedToLoadData: String
    fun simulatorCandleCounter(current: Int, total: Int): String
    val simulatorPrevContentDesc: String
    val simulatorPauseContentDesc: String
    val simulatorPlayContentDesc: String
    val simulatorNextContentDesc: String
    val simulatorStatBalance: String
    val simulatorStatEquity: String
    val simulatorStatPnl: String
    val simulatorNewPositionTitle: String
    val simulatorAmountLabel: String
    val simulatorLeverageLabel: String
    fun simulatorLiquidatesAt(price: String, pct: String): String
    val simulatorStopLossLabel: String
    val simulatorTakeProfitLabel: String
    val simulatorLongAtMarket: String
    val simulatorShortAtMarket: String
    fun simulatorOpenPositions(count: Int): String
    val simulatorMetricsTitle: String
    val simulatorMetricWinRate: String
    val simulatorMetricProfitFactor: String
    val simulatorMetricTrades: String
    val simulatorMetricMaxDD: String
    val simulatorMetricBest: String
    val simulatorMetricSharpe: String
    val simulatorTradeHistoryTitle: String
    fun simulatorEntryLabel(price: String): String
    fun simulatorNowLabel(price: String): String
    fun simulatorPnlLine(pnl: String, pct: String): String
    fun simulatorEntryExitLabel(entry: String, exit: String): String
    fun simulatorSlLabel(price: String): String
    fun simulatorTpLabel(price: String): String
    val simulatorCloseButton: String

    // ── Biometric login screen ──
    val biometricTagline: String
    val biometricLoginButton: String
    val biometricNotAvailable: String
    val biometricDisclaimer: String
    val biometricCreateAccountPrompt: String
    val biometricLoginWithAccountPrompt: String

    // ── Register / Login (email+password account, server-backed) ──
    val authRegisterTitle: String
    val authLoginTitle: String
    val authEmailLabel: String
    val authPasswordLabel: String
    val authUsernameOptionalLabel: String
    val authRegisterButton: String
    val authLoginButton: String
    val authSwitchToLoginPrompt: String
    val authSwitchToRegisterPrompt: String
    val authBackToBiometricPrompt: String
    val authErrorGeneric: String
    val authErrorInvalidInput: String
    val authErrorEmailExists: String

    // ── Bottom navigation ──
    val navDashboard: String
    val navPortfolio: String
    val navHistory: String
    val navProfile: String

    // ── Crypto Library screen chrome (article content stays English-only for now) ──
    val libraryTitle: String
    val librarySubtitle: String
    val libraryTopicNotFound: String
    val libraryLevelBeginner: String
    val libraryLevelIntermediate: String
    val libraryLevelAdvanced: String
}

private object EnStrings : AppStrings {
    override val actionBack = "Back"
    override val actionConfirm = "Confirm"
    override val actionCancel = "Cancel"
    override val actionRetry = "Retry"

    override val profileTitle = "Profile"
    override val profileEditButton = "Edit Profile"
    override val profileStatTotalTrades = "Total Trades"
    override val profileStatWinRate = "Win Rate"
    override val profileStatDaysActive = "Days Active"
    override val profileMenuPersonalInfoTitle = "Personal Information"
    override val profileMenuPersonalInfoSubtitle = "Edit your personal details"
    override val profileMenuNotificationsTitle = "Notifications"
    override val profileMenuNotificationsSubtitle = "Manage your notification preferences"
    override val profileMenuSecurityTitle = "Security & Privacy"
    override val profileMenuSecuritySubtitle = "Protect your account"
    override val profileMenuPnlTitle = "P&L Analytics"
    override val profileMenuPnlSubtitle = "View your trading performance"
    override val profileMenuSponsorshipTitle = "Support Development"
    override val profileMenuSponsorshipSubtitle = "Sponsor the author, buy a coffee"
    override val sponsorshipTitle = "Support KB Learning"
    override val sponsorshipDesc = "KB Learning is an independent project dedicated to providing the best crypto learning experience. Your support helps maintain server infrastructure, develop new features, and keep the application ad-free."
    override val sponsorshipCoffee = "One-time Donation"
    override val sponsorshipCoffeeSub = "Support current development"
    override val sponsorshipBeer = "Monthly Sponsorship"
    override val sponsorshipBeerSub = "Help sustain long-term growth"
    override val profileMenuHelpTitle = "Help & Support"
    override val profileMenuHelpSubtitle = "Get help and contact us"
    override val profileMenuLanguageTitle = "Language"
    override val profileMenuLanguageSubtitle = "Choose your app language"

    override val editProfileTitle = "Edit Profile"
    override val editProfileDisplayNameLabel = "Display Name"
    override val editProfileEmailLabel = "Email Address"
    override val editProfileSaveButton = "Save Changes"
    override val editProfileUpdateSuccess = "Profile updated successfully"

    override val notificationsTitle = "Notifications"
    override val sectionGeneral = "General"
    override val notifPushTitle = "Push Notifications"
    override val notifPushSubtitle = "Receive push notifications"
    override val notifEmailTitle = "Email Notifications"
    override val notifEmailSubtitle = "Receive email updates"
    override val sectionTrading = "Trading"
    override val notifPriceAlertsTitle = "Price Alerts"
    override val notifPriceAlertsSubtitle = "Get notified on price changes"
    override val notifTradeConfirmationsTitle = "Trade Confirmations"
    override val notifTradeConfirmationsSubtitle = "Confirm each trade action"
    override val sectionOther = "Other"
    override val notifNewsTitle = "News & Updates"
    override val notifNewsSubtitle = "Stay informed with latest news"

    override val profileMenuNotificationCenterTitle = "Notification Centre"
    override val profileMenuNotificationCenterSubtitle = "Trade confirmations and alerts"
    override val notifCenterEmptyTitle = "No notifications yet"
    override val notifCenterEmptySubtitle = "Trade confirmations and alerts will show up here"
    override val notifCenterMarkAllRead = "Mark all read"
    override val notifCenterClearAll = "Clear all"
    override val notifSystemBlockedTitle = "System notifications are turned off"
    override val notifSystemBlockedAction = "Enable"
    override val notifPurchaseTitle = "Purchase complete"
    override fun notifPurchaseBody(amount: String, total: String) = "Bought $amount for $total"
    override fun tradeSuccessSubtitle(amount: String) = "$amount added to your portfolio"

    override val securityTitle = "Security"
    override val sectionAuthentication = "Authentication"
    override val securityBiometricTitle = "Biometric Authentication"
    override val securityBiometricSubtitle = "Use fingerprint or face ID to login"
    override val security2faTitle = "Two-Factor Authentication"
    override val security2faSubtitle = "Add an extra layer of security"
    override val sectionAccount = "Account"
    override val securityChangePassword = "Change Password"
    override val dialogCurrentPasswordLabel = "Current Password"
    override val dialogNewPasswordLabel = "New Password"
    override val dialogConfirmPasswordLabel = "Confirm New Password"

    override val helpTitle = "Help & Support"
    override val helpFaqHeading = "FAQ"
    override val faqQ1 = "How do I buy cryptocurrency?"
    override val faqA1 = "Navigate to the Coins screen from Portfolio or Dashboard, select a coin, and use the Buy screen to enter your desired amount."
    override val faqQ2 = "How do I sell my assets?"
    override val faqA2 = "Tap on any coin in your Portfolio, or long-press a coin in the Coins list, and select Sell. Enter the amount you wish to sell."
    override val faqQ3 = "Where can I see my transaction history?"
    override val faqA3 = "All your buy and sell transactions are recorded in the History tab. You can view details like date, amount, and price there."
    override val faqQ4 = "Is my data secure?"
    override val faqA4 = "Yes! KB Learning uses biometric authentication and local encryption to keep your data safe. Enable biometric login in Security settings."
    override val faqQ5 = "How are coin prices determined?"
    override val faqA5 = "Coin prices are fetched from live market data via API. Prices update in real-time to reflect current market conditions."
    override val helpContactHeading = "Contact Us"
    override val helpContactLiveChat = "Live Chat: Available 9AM - 6PM"
    override val helpAboutHeading = "About"
    override val helpAboutVersion = "Version 1.0.0"
    override val helpAboutTagline = "Built with Kotlin Multiplatform"
    override val contentDescCollapse = "Collapse"
    override val contentDescExpand = "Expand"

    override val languageTitle = "Language"
    override val languageEnglish = "English"
    override val languageRussian = "Русский"
    override val languageSystemDefault = "Follows your device language by default"

    override val dashboardTitle = "Dashboard"
    override val dashboardSimulatorButton = "Simulator"
    override val dashboardStatTotalBalance = "Total Balance"
    override val dashboardStatCash = "Cash"
    override val dashboardStatPortfolioValue = "In Assets"
    override val dashboardStatAssets = "Assets"
    override val dashboardStat24hChange = "24h Change"
    override val dashboardMarketOverview = "Market Overview"
    override val dashboardTradingTipTitle = "📈 Trading Tip"
    override val dashboardTradingTipBody = "Start by exploring available coins and buying your first cryptocurrency. Diversify your portfolio to manage risk effectively."
    override val dashboardLibraryTitle = "📚 Crypto Library"
    override val dashboardLibrarySubtitle = "New to crypto? Learn everything you need to know, from the basics to advanced topics."
    override val dashboardPortfolioSummary = "Portfolio Summary"
    override val dashboardNoAssetsTitle = "No assets yet"
    override val dashboardNoAssetsSubtitle = "Go to Portfolio → Discover Coins to start trading"
    override fun dashboardAssetsCount(count: Int) = "$count asset(s) in portfolio"

    override val portfolioTitle = "Portfolio"
    override val portfolioBalanceLabel = "Total Balance"
    override val portfolioCashLabel = "Cash"
    override val portfolioHoldingsLabel = "In assets"
    override val portfolioDiscoverCoinsButton = "Discover Coins"
    override val portfolioSearchPlaceholder = "Search coins..."
    override val portfolioDistributionTitle = "Portfolio Distribution"
    override val portfolioNothingToShow = "Nothing to show yet. Add some coins!"
    override val portfolioYourAssets = "Your Assets"
    override fun portfolioCoinsCount(count: Int) = "$count coins"
    override val portfolioNoSearchResults = "No coins match your search"
    override val portfolioEmptyTitle = "Your portfolio is empty"
    override val portfolioEmptySubtitle = "Start by discovering coins to trade"

    override val historyTitle = "Transaction History"
    override val historyStatTotalTrades = "Total Trades"
    override val historyStatTotalBuy = "Total Buy"
    override val historyStatTotalSell = "Total Sell"
    override val historyRecentTransactions = "Recent Transactions"
    override val historyEmptyTitle = "No transactions yet"
    override val historyEmptySubtitle = "Your buy and sell history will appear here"
    override val historyBuyLabel = "Buy"
    override val historySellLabel = "Sell"
    override fun historyPriceLabel(price: String) = "Price: $price"

    override val watchlistTitle = "Watchlist"
    override val watchlistEmptyTitle = "No items in watchlist"
    override val watchlistEmptySubtitle = "Add coins from the market screen"

    override val coinsListTitle = "Popular Coins"

    override val chartModeCandles = "Candles"
    override val chartModeLine = "Line"

    override val pnlTitle = "P&L Analytics"
    override val pnlStatTotalTrades = "Total Trades"
    override val pnlStatWinRate = "Win Rate"
    override val pnlStatActiveOrders = "Active Orders"
    override val pnlRealized = "Realized P&L"
    override val pnlDetailedStats = "Detailed Statistics"
    override val pnlRowTotalInvested = "Total Invested"
    override val pnlRowTotalRealized = "Total Realized"
    override val pnlRowBestTrade = "Best Trade"
    override val pnlRowWorstTrade = "Worst Trade"
    override val pnlRowAvgProfitPerTrade = "Avg Profit/Trade"
    override val pnlRowBuyOrders = "Buy Orders"
    override val pnlRowSellOrders = "Sell Orders"

    override val tradeCoinFallback = "Coin"
    override val tradeCoinAmountLabel = "Coin Amount"
    override val tradeBuyAmountLabel = "Buy Amount"
    override val tradeSellAmountLabel = "Sell Amount"
    override val tradeBuyButton = "Buy"
    override val tradeSellButton = "Sell"
    override val tradeAvailablePrefix = "Available: "

    override val simulatorTitle = "Trading Simulator"
    override val simulatorHintContentDesc = "Hint"
    override val simulatorSelectCoinPrompt = "Select a coin to simulate:"
    override val simulatorErrorFailedToLoadCoins = "Failed to load coins"
    override val simulatorErrorNotEnoughData = "Not enough price history for this coin"
    override val simulatorErrorFailedToLoadData = "Failed to load price data"
    override fun simulatorCandleCounter(current: Int, total: Int) = "Candle $current/$total"
    override val simulatorPrevContentDesc = "Prev"
    override val simulatorPauseContentDesc = "Pause"
    override val simulatorPlayContentDesc = "Play"
    override val simulatorNextContentDesc = "Next"
    override val simulatorStatBalance = "Balance"
    override val simulatorStatEquity = "Equity"
    override val simulatorStatPnl = "P&L"
    override val simulatorNewPositionTitle = "New Position"
    override val simulatorAmountLabel = "Amount ($)"
    override val simulatorLeverageLabel = "Leverage (x)"
    override fun simulatorLiquidatesAt(price: String, pct: String) = "Liquidates at ~\$$price ($pct% adverse move)"
    override val simulatorStopLossLabel = "Stop-Loss ($)"
    override val simulatorTakeProfitLabel = "Take-Profit ($)"
    override val simulatorLongAtMarket = "Long at Market"
    override val simulatorShortAtMarket = "Short at Market"
    override fun simulatorOpenPositions(count: Int) = "Open Positions ($count)"
    override val simulatorMetricsTitle = "Metrics"
    override val simulatorMetricWinRate = "Win Rate"
    override val simulatorMetricProfitFactor = "Profit Factor"
    override val simulatorMetricTrades = "Trades"
    override val simulatorMetricMaxDD = "Max DD"
    override val simulatorMetricBest = "Best"
    override val simulatorMetricSharpe = "Sharpe"
    override val simulatorTradeHistoryTitle = "Trade History"
    override fun simulatorEntryLabel(price: String) = "Entry: $price"
    override fun simulatorNowLabel(price: String) = "Now: $price"
    override fun simulatorPnlLine(pnl: String, pct: String) = "P&L: $pnl ($pct%)"
    override fun simulatorEntryExitLabel(entry: String, exit: String) = "Entry: $entry → Exit: $exit"
    override fun simulatorSlLabel(price: String) = "SL: $price"
    override fun simulatorTpLabel(price: String) = "TP: $price"
    override val simulatorCloseButton = "Close"

    override val biometricTagline = "Powered by Compose Multiplatform"
    override val biometricLoginButton = "Login"
    override val biometricNotAvailable = "Biometric is not available on your device!"
    override val biometricDisclaimer = "This app is for educational purposes only and does not constitute financial advice. Cryptocurrency investments carry risk — do your own research."
    override val biometricCreateAccountPrompt = "No biometrics? Create an account"
    override val biometricLoginWithAccountPrompt = "Already have an account? Log in"

    override val authRegisterTitle = "Create your account"
    override val authLoginTitle = "Log in"
    override val authEmailLabel = "Email"
    override val authPasswordLabel = "Password"
    override val authUsernameOptionalLabel = "Username (optional)"
    override val authRegisterButton = "Create account"
    override val authLoginButton = "Log in"
    override val authSwitchToLoginPrompt = "Already have an account? Log in"
    override val authSwitchToRegisterPrompt = "Don't have an account? Create one"
    override val authBackToBiometricPrompt = "Back to biometric login"
    override val authErrorGeneric = "Something went wrong. Check your details and try again."
    override val authErrorInvalidInput = "Enter a valid email and a password of at least 6 characters."
    override val authErrorEmailExists = "An account with this email already exists."

    override val navDashboard = "Dashboard"
    override val navPortfolio = "Portfolio"
    override val navHistory = "History"
    override val navProfile = "Profile"

    override val libraryTitle = "Crypto Library"
    override val librarySubtitle = "Everything a beginner should know, start to finish"
    override val libraryTopicNotFound = "Topic not found"
    override val libraryLevelBeginner = "Beginner"
    override val libraryLevelIntermediate = "Intermediate"
    override val libraryLevelAdvanced = "Advanced"
}

private object RuStrings : AppStrings {
    override val actionBack = "Назад"
    override val actionConfirm = "Подтвердить"
    override val actionCancel = "Отмена"
    override val actionRetry = "Повторить"

    override val profileTitle = "Профиль"
    override val profileEditButton = "Редактировать профиль"
    override val profileStatTotalTrades = "Всего сделок"
    override val profileStatWinRate = "Процент прибыльных сделок"
    override val profileStatDaysActive = "Дней в приложении"
    override val profileMenuPersonalInfoTitle = "Личные данные"
    override val profileMenuPersonalInfoSubtitle = "Изменить личные данные"
    override val profileMenuNotificationsTitle = "Уведомления"
    override val profileMenuNotificationsSubtitle = "Настройка уведомлений"
    override val profileMenuSecurityTitle = "Безопасность и конфиденциальность"
    override val profileMenuSecuritySubtitle = "Защитите свой аккаунт"
    override val profileMenuPnlTitle = "Аналитика P&L"
    override val profileMenuPnlSubtitle = "Статистика вашей торговли"
    override val profileMenuSponsorshipTitle = "Поддержать проект"
    override val profileMenuSponsorshipSubtitle = "Спонсировать автора, купить кофе"
    override val sponsorshipTitle = "Поддержать проект"
    override val sponsorshipDesc = "KB Learning — это независимый проект, созданный для комфортного обучения. Ваша поддержка помогает оплачивать серверную инфраструктуру, разрабатывать новые функции и сохранять приложение без рекламы."
    override val sponsorshipCoffee = "Разовое пожертвование"
    override val sponsorshipCoffeeSub = "Поддержать текущую разработку"
    override val sponsorshipBeer = "Ежемесячная поддержка"
    override val sponsorshipBeerSub = "Помочь долгосрочному развитию"
    override val profileMenuHelpTitle = "Помощь и поддержка"
    override val profileMenuHelpSubtitle = "Получить помощь и связаться с нами"
    override val profileMenuLanguageTitle = "Язык"
    override val profileMenuLanguageSubtitle = "Выберите язык приложения"

    override val editProfileTitle = "Редактировать профиль"
    override val editProfileDisplayNameLabel = "Отображаемое имя"
    override val editProfileEmailLabel = "Адрес электронной почты"
    override val editProfileSaveButton = "Сохранить изменения"
    override val editProfileUpdateSuccess = "Профиль успешно обновлён"

    override val notificationsTitle = "Уведомления"
    override val sectionGeneral = "Основные"
    override val notifPushTitle = "Push-уведомления"
    override val notifPushSubtitle = "Получать push-уведомления"
    override val notifEmailTitle = "Уведомления по почте"
    override val notifEmailSubtitle = "Получать обновления по email"
    override val sectionTrading = "Торговля"
    override val notifPriceAlertsTitle = "Оповещения о цене"
    override val notifPriceAlertsSubtitle = "Уведомлять об изменении цены"
    override val notifTradeConfirmationsTitle = "Подтверждение сделок"
    override val notifTradeConfirmationsSubtitle = "Подтверждать каждое действие по сделке"
    override val sectionOther = "Другое"
    override val notifNewsTitle = "Новости и обновления"
    override val notifNewsSubtitle = "Быть в курсе последних новостей"

    override val profileMenuNotificationCenterTitle = "Центр уведомлений"
    override val profileMenuNotificationCenterSubtitle = "Подтверждения сделок и оповещения"
    override val notifCenterEmptyTitle = "Уведомлений пока нет"
    override val notifCenterEmptySubtitle = "Здесь появятся подтверждения сделок и оповещения"
    override val notifCenterMarkAllRead = "Отметить прочитанными"
    override val notifCenterClearAll = "Очистить"
    override val notifSystemBlockedTitle = "Системные уведомления отключены"
    override val notifSystemBlockedAction = "Включить"
    override val notifPurchaseTitle = "Покупка совершена"
    override fun notifPurchaseBody(amount: String, total: String) = "Куплено $amount на $total"
    override fun tradeSuccessSubtitle(amount: String) = "$amount добавлено в портфель"

    override val securityTitle = "Безопасность"
    override val sectionAuthentication = "Аутентификация"
    override val securityBiometricTitle = "Биометрическая аутентификация"
    override val securityBiometricSubtitle = "Вход по отпечатку пальца или Face ID"
    override val security2faTitle = "Двухфакторная аутентификация"
    override val security2faSubtitle = "Дополнительный уровень защиты"
    override val sectionAccount = "Аккаунт"
    override val securityChangePassword = "Изменить пароль"
    override val dialogCurrentPasswordLabel = "Текущий пароль"
    override val dialogNewPasswordLabel = "Новый пароль"
    override val dialogConfirmPasswordLabel = "Подтвердите новый пароль"

    override val helpTitle = "Помощь и поддержка"
    override val helpFaqHeading = "Частые вопросы"
    override val faqQ1 = "Как купить криптовалюту?"
    override val faqA1 = "Перейдите на экран «Монеты» из раздела «Портфель» или «Главная», выберите монету и укажите нужную сумму на экране покупки."
    override val faqQ2 = "Как продать свои активы?"
    override val faqA2 = "Нажмите на монету в вашем портфеле или удерживайте монету в списке «Монеты» и выберите «Продать». Укажите сумму, которую хотите продать."
    override val faqQ3 = "Где посмотреть историю операций?"
    override val faqA3 = "Все операции покупки и продажи фиксируются на вкладке «История». Там видны дата, сумма и цена каждой операции."
    override val faqQ4 = "Безопасны ли мои данные?"
    override val faqA4 = "Да! KB Learning использует биометрическую аутентификацию и локальное шифрование для защиты ваших данных. Включите биометрический вход в настройках безопасности."
    override val faqQ5 = "Как определяются цены на монеты?"
    override val faqA5 = "Цены на монеты берутся из актуальных рыночных данных через API и обновляются в реальном времени."
    override val helpContactHeading = "Связаться с нами"
    override val helpContactLiveChat = "Онлайн-чат: доступен с 9:00 до 18:00"
    override val helpAboutHeading = "О приложении"
    override val helpAboutVersion = "Версия 1.0.0"
    override val helpAboutTagline = "Создано на Kotlin Multiplatform"
    override val contentDescCollapse = "Свернуть"
    override val contentDescExpand = "Развернуть"

    override val languageTitle = "Язык"
    override val languageEnglish = "English"
    override val languageRussian = "Русский"
    override val languageSystemDefault = "По умолчанию соответствует языку устройства"

    override val dashboardTitle = "Главная"
    override val dashboardSimulatorButton = "Симулятор"
    override val dashboardStatTotalBalance = "Общий баланс"
    override val dashboardStatCash = "Свободные средства"
    override val dashboardStatPortfolioValue = "В активах"
    override val dashboardStatAssets = "Активы"
    override val dashboardStat24hChange = "Изменение за 24ч"
    override val dashboardMarketOverview = "Обзор рынка"
    override val dashboardTradingTipTitle = "📈 Совет трейдеру"
    override val dashboardTradingTipBody = "Начните с изучения доступных монет и покупки первой криптовалюты. Диверсифицируйте портфель, чтобы эффективно управлять риском."
    override val dashboardLibraryTitle = "📚 Крипто-библиотека"
    override val dashboardLibrarySubtitle = "Новичок в крипте? Узнайте всё необходимое — от основ до продвинутых тем."
    override val dashboardPortfolioSummary = "Сводка по портфелю"
    override val dashboardNoAssetsTitle = "Пока нет активов"
    override val dashboardNoAssetsSubtitle = "Перейдите в Портфель → Найти монеты, чтобы начать торговать"
    override fun dashboardAssetsCount(count: Int) = "Активов в портфеле: $count"

    override val portfolioTitle = "Портфель"
    override val portfolioBalanceLabel = "Общий баланс"
    override val portfolioCashLabel = "Свободные средства"
    override val portfolioHoldingsLabel = "В активах"
    override val portfolioDiscoverCoinsButton = "Найти монеты"
    override val portfolioSearchPlaceholder = "Поиск монет..."
    override val portfolioDistributionTitle = "Распределение портфеля"
    override val portfolioNothingToShow = "Пока нечего показать. Добавьте монеты!"
    override val portfolioYourAssets = "Ваши активы"
    override fun portfolioCoinsCount(count: Int) = "монет: $count"
    override val portfolioNoSearchResults = "Монеты не найдены"
    override val portfolioEmptyTitle = "Ваш портфель пуст"
    override val portfolioEmptySubtitle = "Начните с поиска монет для торговли"

    override val historyTitle = "История операций"
    override val historyStatTotalTrades = "Всего сделок"
    override val historyStatTotalBuy = "Всего покупок"
    override val historyStatTotalSell = "Всего продаж"
    override val historyRecentTransactions = "Последние операции"
    override val historyEmptyTitle = "Пока нет операций"
    override val historyEmptySubtitle = "Здесь появится история ваших покупок и продаж"
    override val historyBuyLabel = "Покупка"
    override val historySellLabel = "Продажа"
    override fun historyPriceLabel(price: String) = "Цена: $price"

    override val watchlistTitle = "Список наблюдения"
    override val watchlistEmptyTitle = "Список наблюдения пуст"
    override val watchlistEmptySubtitle = "Добавьте монеты с экрана рынка"

    override val coinsListTitle = "Популярные монеты"

    override val chartModeCandles = "Свечи"
    override val chartModeLine = "Линия"

    override val pnlTitle = "Аналитика P&L"
    override val pnlStatTotalTrades = "Всего сделок"
    override val pnlStatWinRate = "Процент прибыльных"
    override val pnlStatActiveOrders = "Активных ордеров"
    override val pnlRealized = "Реализованный P&L"
    override val pnlDetailedStats = "Подробная статистика"
    override val pnlRowTotalInvested = "Всего инвестировано"
    override val pnlRowTotalRealized = "Всего реализовано"
    override val pnlRowBestTrade = "Лучшая сделка"
    override val pnlRowWorstTrade = "Худшая сделка"
    override val pnlRowAvgProfitPerTrade = "Средняя прибыль/сделка"
    override val pnlRowBuyOrders = "Ордеров на покупку"
    override val pnlRowSellOrders = "Ордеров на продажу"

    override val tradeCoinFallback = "Монета"
    override val tradeCoinAmountLabel = "Количество монет"
    override val tradeBuyAmountLabel = "Сумма покупки"
    override val tradeSellAmountLabel = "Сумма продажи"
    override val tradeBuyButton = "Купить"
    override val tradeSellButton = "Продать"
    override val tradeAvailablePrefix = "Доступно: "

    override val simulatorTitle = "Торговый симулятор"
    override val simulatorHintContentDesc = "Подсказка"
    override val simulatorSelectCoinPrompt = "Выберите монету для симуляции:"
    override val simulatorErrorFailedToLoadCoins = "Не удалось загрузить список монет"
    override val simulatorErrorNotEnoughData = "Недостаточно истории цен для этой монеты"
    override val simulatorErrorFailedToLoadData = "Не удалось загрузить данные о цене"
    override fun simulatorCandleCounter(current: Int, total: Int) = "Свеча $current/$total"
    override val simulatorPrevContentDesc = "Назад"
    override val simulatorPauseContentDesc = "Пауза"
    override val simulatorPlayContentDesc = "Играть"
    override val simulatorNextContentDesc = "Вперёд"
    override val simulatorStatBalance = "Баланс"
    override val simulatorStatEquity = "Капитал"
    override val simulatorStatPnl = "P&L"
    override val simulatorNewPositionTitle = "Новая позиция"
    override val simulatorAmountLabel = "Сумма ($)"
    override val simulatorLeverageLabel = "Плечо (x)"
    override fun simulatorLiquidatesAt(price: String, pct: String) = "Ликвидация на ~\$$price (движение на $pct% против позиции)"
    override val simulatorStopLossLabel = "Стоп-лосс ($)"
    override val simulatorTakeProfitLabel = "Тейк-профит ($)"
    override val simulatorLongAtMarket = "Лонг по рынку"
    override val simulatorShortAtMarket = "Шорт по рынку"
    override fun simulatorOpenPositions(count: Int) = "Открытые позиции ($count)"
    override val simulatorMetricsTitle = "Метрики"
    override val simulatorMetricWinRate = "Процент побед"
    override val simulatorMetricProfitFactor = "Фактор прибыли"
    override val simulatorMetricTrades = "Сделок"
    override val simulatorMetricMaxDD = "Макс. просадка"
    override val simulatorMetricBest = "Лучшая"
    override val simulatorMetricSharpe = "Шарп"
    override val simulatorTradeHistoryTitle = "История сделок"
    override fun simulatorEntryLabel(price: String) = "Вход: $price"
    override fun simulatorNowLabel(price: String) = "Сейчас: $price"
    override fun simulatorPnlLine(pnl: String, pct: String) = "P&L: $pnl ($pct%)"
    override fun simulatorEntryExitLabel(entry: String, exit: String) = "Вход: $entry → Выход: $exit"
    override fun simulatorSlLabel(price: String) = "SL: $price"
    override fun simulatorTpLabel(price: String) = "TP: $price"
    override val simulatorCloseButton = "Закрыть"

    override val biometricTagline = "Работает на Compose Multiplatform"
    override val biometricLoginButton = "Войти"
    override val biometricNotAvailable = "Биометрия недоступна на вашем устройстве!"
    override val biometricDisclaimer = "Это приложение создано в образовательных целях и не является финансовой консультацией. Инвестиции в криптовалюту сопряжены с риском — проводите собственное исследование."
    override val biometricCreateAccountPrompt = "Нет биометрии? Создать аккаунт"
    override val biometricLoginWithAccountPrompt = "Уже есть аккаунт? Войти"

    override val authRegisterTitle = "Создайте аккаунт"
    override val authLoginTitle = "Вход"
    override val authEmailLabel = "Email"
    override val authPasswordLabel = "Пароль"
    override val authUsernameOptionalLabel = "Имя пользователя (необязательно)"
    override val authRegisterButton = "Создать аккаунт"
    override val authLoginButton = "Войти"
    override val authSwitchToLoginPrompt = "Уже есть аккаунт? Войти"
    override val authSwitchToRegisterPrompt = "Нет аккаунта? Создать"
    override val authBackToBiometricPrompt = "Назад ко входу по биометрии"
    override val authErrorGeneric = "Что-то пошло не так. Проверьте данные и попробуйте снова."
    override val authErrorInvalidInput = "Введите корректный email и пароль не короче 6 символов."
    override val authErrorEmailExists = "Аккаунт с таким email уже существует."

    override val navDashboard = "Главная"
    override val navPortfolio = "Портфель"
    override val navHistory = "История"
    override val navProfile = "Профиль"

    override val libraryTitle = "Крипто-библиотека"
    override val librarySubtitle = "Всё, что должен знать новичок, от начала до конца"
    override val libraryTopicNotFound = "Тема не найдена"
    override val libraryLevelBeginner = "Начальный"
    override val libraryLevelIntermediate = "Средний"
    override val libraryLevelAdvanced = "Продвинутый"
}

/**
 * Catalog lookup outside the composition — for layers that have to produce user
 * copy without a Composable scope (notifications posted from a ViewModel or the
 * OS notifier). Inside the UI use [appStrings] instead, so the text recomposes
 * when the language changes.
 */
fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.RUSSIAN -> RuStrings
    AppLanguage.ENGLISH -> EnStrings
}

@Composable
fun appStrings(): AppStrings = stringsFor(LocalAppLanguage.current)
