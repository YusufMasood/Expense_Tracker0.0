package com.yusuf.expensepro.presentation.ui.auth.login

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.yusuf.expensepro.data.repository.AuthRepository
import com.yusuf.expensepro.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Pre-check: if already logged in, auto-navigate
    val isAlreadyLoggedIn: Boolean get() = authRepository.isLoggedIn

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null, error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, passwordError = null, error = null) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun login() {
        val state = _uiState.value
        var hasError = false
        if (state.email.isBlank()) { _uiState.update { it.copy(emailError = "Email is required") }; hasError = true }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) { _uiState.update { it.copy(emailError = "Enter a valid email") }; hasError = true }
        if (state.password.length < 6) { _uiState.update { it.copy(passwordError = "Min 6 characters") }; hasError = true }
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(state.email.trim(), state.password)) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AuthResult.Error  -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun handleGoogleSignInResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            _uiState.update { it.copy(isGoogleLoading = false, error = "Google sign-in cancelled") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true) }
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                when (val r = authRepository.signInWithGoogle(account)) {
                    is AuthResult.Success -> _uiState.update { it.copy(isGoogleLoading = false, isSuccess = true) }
                    is AuthResult.Error  -> _uiState.update { it.copy(isGoogleLoading = false, error = r.message) }
                }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isGoogleLoading = false, error = "Google sign-in failed: ${e.statusCode}") }
            }
        }
    }
}
