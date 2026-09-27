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

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: AuthError? = null,
)

sealed interface LoginEvent {
    data object Success : LoginEvent
}

class LoginViewModel(
    private val authApiClient: AuthApiClient,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChanged(email: String) = _state.update { it.copy(email = email, error = null) }
    fun onPasswordChanged(password: String) = _state.update { it.copy(password = password, error = null) }

    fun onLoginClicked() {
        val s = _state.value
        if (s.isLoading) return
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = AuthError.INVALID_INPUT) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authApiClient.login(email = s.email.trim(), password = s.password)
            when (result) {
                is Result.Success -> {
                    tokenStorage.saveTokens(result.data.accessToken, result.data.refreshToken)
                    tokenStorage.setUserId(result.data.user.id)
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.Success)
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
}
