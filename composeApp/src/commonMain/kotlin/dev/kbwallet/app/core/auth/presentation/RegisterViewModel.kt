package dev.kbwallet.app.core.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.domain.user.AuthError
import dev.kbwallet.app.core.network.auth.AuthApiClient
import dev.kbwallet.app.core.security.TokenStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterState(
    val email: String = "",
    val password: String = "",
    // Everything past email+password is optional — see RegisterRequest's doc
    // comment. Left blank, the server generates a default handle.
    val username: String = "",
    val isLoading: Boolean = false,
    val error: AuthError? = null,
)

sealed interface RegisterEvent {
    data object Success : RegisterEvent
}

class RegisterViewModel(
    private val authApiClient: AuthApiClient,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChanged(email: String) = _state.update { it.copy(email = email, error = null) }
    fun onPasswordChanged(password: String) = _state.update { it.copy(password = password, error = null) }
    fun onUsernameChanged(username: String) = _state.update { it.copy(username = username, error = null) }

    fun onRegisterClicked() {
        val s = _state.value
        if (s.isLoading) return

        // Same minimal bar the server enforces (see AuthRoutes.register) —
        // catches obviously-bad input before a round trip, not a replacement
        // for the server's own validation.
        if (s.email.isBlank() || !s.email.contains("@") || s.password.length < 6) {
            _state.update { it.copy(error = AuthError.INVALID_INPUT) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authApiClient.register(
                email = s.email.trim(),
                password = s.password,
                username = s.username.trim().ifBlank { null },
            )
            when (result) {
                is Result.Success -> {
                    tokenStorage.saveTokens(result.data.accessToken, result.data.refreshToken)
                    tokenStorage.setUserId(result.data.user.id)
                    _state.update { it.copy(isLoading = false) }
                    _events.send(RegisterEvent.Success)
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
}
