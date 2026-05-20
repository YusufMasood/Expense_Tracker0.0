package com.yusuf.expensepro.presentation.ui.split

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.SplitRepository
import com.yusuf.expensepro.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupWithBalance(
    val group: SplitGroup,
    val myNet: Double  // positive = I receive, negative = I owe
)

data class SplitListUiState(
    val groupsWithBalance: List<GroupWithBalance> = emptyList(),
    val totalToReceive: Double = 0.0,
    val totalOwed: Double = 0.0,
    val showCreateDialog: Boolean = false,
    val newGroupName: String = "",
    val newGroupIcon: String = "👥",
    val isLoading: Boolean = true
)

@HiltViewModel
class SplitViewModel @Inject constructor(
    private val splitRepository: SplitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitListUiState())
    val uiState: StateFlow<SplitListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            splitRepository.getAllGroups().collect { groups ->
                val withBalances = groups.map { group ->
                    val balances = splitRepository.getMemberBalances(group.id)
                    val me = balances.find { it.member.isCurrentUser }
                    GroupWithBalance(group, me?.net ?: 0.0)
                }
                val toReceive = withBalances.filter { it.myNet > 0 }.sumOf { it.myNet }
                val owes = withBalances.filter { it.myNet < 0 }.sumOf { -it.myNet }
                _uiState.update {
                    it.copy(
                        groupsWithBalance = withBalances,
                        totalToReceive = toReceive,
                        totalOwed = owes,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showCreateDialog() = _uiState.update { it.copy(showCreateDialog = true, newGroupName = "", newGroupIcon = "👥") }
    fun hideCreateDialog() = _uiState.update { it.copy(showCreateDialog = false) }
    fun onGroupNameChange(v: String) = _uiState.update { it.copy(newGroupName = v) }
    fun onGroupIconChange(v: String) = _uiState.update { it.copy(newGroupIcon = v) }

    fun createGroup() {
        val state = _uiState.value
        if (state.newGroupName.isBlank()) return
        viewModelScope.launch {
            val groupId = splitRepository.insertGroup(
                SplitGroup(name = state.newGroupName.trim(), icon = state.newGroupIcon)
            )
            // Auto-add current user as "Me"
            splitRepository.insertMember(
                SplitMember(groupId = groupId, name = "Me", isCurrentUser = true)
            )
            _uiState.update { it.copy(showCreateDialog = false) }
        }
    }

    fun deleteGroup(group: SplitGroup) {
        viewModelScope.launch { splitRepository.deleteGroup(group) }
    }
}
