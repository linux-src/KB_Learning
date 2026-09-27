package dev.kbwallet.app.core.navigation

import kotlinx.serialization.Serializable

// ── App entry ──
@Serializable
object Biometric

@Serializable
object Register

@Serializable
object Login

// ── Main tabs (BottomNavigationBar) ──
@Serializable
object Dashboard

@Serializable
object Portfolio

@Serializable
object History

@Serializable
object Profile

// ── Secondary screens ──
@Serializable
object Coins

@Serializable
data class CryptoChart(val coinId: String, val coinName: String)

// ── Profile sub-screens ──
@Serializable
object EditProfile

@Serializable
object NotificationSettings

/** The in-app notification centre — history of everything the app raised. */
@Serializable
object NotificationCenter

@Serializable
object SecuritySettings

@Serializable
object HelpSupport

@Serializable
object LanguageSettings

@Serializable
object Sponsorship

// ── Trading Simulator additions ──
@Serializable
object PnLAnalytics

@Serializable
object ActiveOrders

@Serializable
object Simulator

// ── Knowledge library ──
@Serializable
object Library

@Serializable
data class Topic(val topicId: String)
