package dev.kbwallet.app.core.auth.presentation

import dev.kbwallet.app.core.domain.user.AuthError
import dev.kbwallet.app.core.i18n.AppStrings

/** Same pattern as SimulatorErrorType.label/LibraryLevel.label — the network layer can't depend on Compose/i18n. */
fun AuthError.label(strings: AppStrings): String = when (this) {
    AuthError.EMAIL_EXISTS -> strings.authErrorEmailExists
    AuthError.INVALID_CREDENTIALS -> strings.authErrorGeneric
    AuthError.INVALID_INPUT -> strings.authErrorInvalidInput
    AuthError.NETWORK -> strings.authErrorGeneric
}
