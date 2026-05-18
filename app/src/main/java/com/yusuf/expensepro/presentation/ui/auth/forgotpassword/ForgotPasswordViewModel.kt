package com.yusuf.expensepro.presentation.ui.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.AuthRepository
import com.yusuf.expensepro.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(val email: String = "", val isLoading: Boolean = false, val error: String? = null, val isSuccess: Boolean = false, val emailError: String? = null)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null, error = null) }

    fun sendReset() {
        val email = _uiState.value.email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { _uiState.update { it.copy(emailError = "Enter a valid email") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = authRepository.sendPasswordReset(email.trim())) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AuthResult.Error   -> _uiState.update { it.copy(isLoading = false, error = r.message) }
            }
        }
    }
}
