package dev.kbwallet.app.theme

import androidx.compose.runtime.Composable

/** Keeps status/navigation bar icons readable against the current theme. */
@Composable
expect fun SystemBarsAppearance(darkTheme: Boolean)
