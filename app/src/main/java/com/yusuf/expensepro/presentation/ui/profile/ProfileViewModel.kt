package com.yusuf.expensepro.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.AuthRepository
import com.yusuf.expensepro.data.repository.AuthResult
import com.yusuf.expensepro.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editPhone: String = "",
    val isLoading: Boolean = false,
    val updateError: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.userProfile.collect { profile ->
                _uiState.update { it.copy(profile = profile) }
            }
        }
    }

    fun startEdit() {
        val p = _uiState.value.profile ?: return
        _uiState.update { it.copy(isEditing = true, editName = p.fullName, editPhone = p.phoneNumber, updateError = null) }
    }

    fun cancelEdit() = _uiState.update { it.copy(isEditing = false) }
    fun onNameChange(v: String) = _uiState.update { it.copy(editName = v) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(editPhone = v) }

    fun saveProfile() {
        val state = _uiState.value
        if (state.editName.isBlank()) { _uiState.update { it.copy(updateError = "Name is required") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.updateProfile(state.editName.trim(), state.editPhone.trim())) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isEditing = false) }
                is AuthResult.Error   -> _uiState.update { it.copy(isLoading = false, updateError = result.message) }
            }
        }
    }

    fun logout() = authRepository.logout()
}
