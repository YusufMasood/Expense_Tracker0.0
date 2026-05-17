package com.yusuf.expensepro.presentation.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.AuthRepository
import com.yusuf.expensepro.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "", val email: String = "",
    val password: String = "", val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false, val isConfirmVisible: Boolean = false,
    val isLoading: Boolean = false, val error: String? = null, val isSuccess: Boolean = false,
    val nameError: String? = null, val emailError: String? = null,
    val passwordError: String? = null, val confirmError: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v, nameError = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null, error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, passwordError = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, confirmError = null) }
    fun togglePassword() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun toggleConfirm() = _uiState.update { it.copy(isConfirmVisible = !it.isConfirmVisible) }

    fun register() {
        val s = _uiState.value; var err = false
        if (s.name.isBlank()) { _uiState.update {
            it.copy(nameError = "Name required") }; err = true }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.email)
            .matches()) { _uiState.update {
                it.copy(emailError = "Valid email required") }; err = true }
        if (s.password.length < 6) { _uiState.update {
            it.copy(passwordError = "Min 6 characters") }; err = true }
        if (s.password != s.confirmPassword)
        { _uiState.update { it.copy(confirmError = "Passwords don't match") }; err = true }
        if (err) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(s.email.trim(), s.password, s.name.trim())) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}
