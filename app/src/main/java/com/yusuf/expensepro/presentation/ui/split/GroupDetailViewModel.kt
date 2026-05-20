package com.yusuf.expensepro.presentation.ui.split

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.ExpenseRepository
import com.yusuf.expensepro.data.repository.SplitRepository
import com.yusuf.expensepro.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExpenseWithShares(
    val expense: SplitExpense,
    val shares: List<SplitShare>,
    val paidByName: String
)

data class GroupDetailUiState(
    val group: SplitGroup? = null,
    val members: List<SplitMember> = emptyList(),
    val expensesWithShares: List<ExpenseWithShares> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val memberBalances: List<MemberBalance> = emptyList(),
    val showAddMemberDialog: Boolean = false,
    val newMemberName: String = "",
    val showAddExpenseSheet: Boolean = false,
    val expenseTitle: String = "",
    val expenseAmount: String = "",
    val expensePaidBy: Long = -1L,
    val expenseCategory: Category = Category.OTHER,
    val expenseSplitType: SplitType = SplitType.EQUAL,
    val memberSplitInputs: List<MemberSplitInput> = emptyList(),
    val expenseTitleError: String? = null,
    val expenseAmountError: String? = null,
    val splitInputError: String? = null,
    val showSettleSheet: Boolean = false,
    val settleTargetMember: SplitMember? = null,
    val settleAmount: String = "",
    val settleNote: String = "",
    val settleAmountError: String? = null,
    val isPartialSettle: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val splitRepository: SplitRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _groupId = MutableStateFlow(-1L)
    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun loadGroup(groupId: Long) {
        if (_groupId.value == groupId) return
        _groupId.value = groupId
        viewModelScope.launch {
            val group = splitRepository.getGroupById(groupId) ?: return@launch
            _uiState.update { it.copy(group = group) }
            combine(
                splitRepository.getMembersForGroup(groupId),
                splitRepository.getExpensesForGroup(groupId),
                splitRepository.getSettlementsForGroup(groupId)
            ) { members, expenses, settlements ->
                val expensesWithShares = expenses.map { exp ->
                    val shares = splitRepository.getSharesForExpenseSync(exp.id)
                    val paidByName = members.find { it.id == exp.paidByMemberId }?.name ?: "Unknown"
                    ExpenseWithShares(exp, shares, paidByName)
                }
                val balances = splitRepository.getMemberBalances(groupId)
                _uiState.update {
                    it.copy(
                        members = members,
                        expensesWithShares = expensesWithShares,
                        settlements = settlements,
                        memberBalances = balances,
                        expensePaidBy = members.find { m -> m.isCurrentUser }?.id ?: members.firstOrNull()?.id ?: -1L,
                        memberSplitInputs = members.map { m -> MemberSplitInput(m) },
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    // ── Add Member ────────────────────────────────────────────────────────────
    fun showAddMemberDialog() = _uiState.update { it.copy(showAddMemberDialog = true, newMemberName = "") }
    fun hideAddMemberDialog() = _uiState.update { it.copy(showAddMemberDialog = false) }
    fun onNewMemberNameChange(v: String) = _uiState.update { it.copy(newMemberName = v) }

    fun addMember() {
        val name = _uiState.value.newMemberName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            splitRepository.insertMember(SplitMember(groupId = _groupId.value, name = name))
            _uiState.update { it.copy(showAddMemberDialog = false) }
        }
    }

    fun deleteMember(member: SplitMember) = viewModelScope.launch { splitRepository.deleteMember(member) }

    // ── Add Expense ───────────────────────────────────────────────────────────
    fun showAddExpenseSheet() = _uiState.update {
        it.copy(
            showAddExpenseSheet = true, expenseTitle = "", expenseAmount = "",
            expenseTitleError = null, expenseAmountError = null, splitInputError = null,
            expenseCategory = Category.OTHER, expenseSplitType = SplitType.EQUAL,
            expensePaidBy = it.members.find { m -> m.isCurrentUser }?.id ?: it.members.firstOrNull()?.id ?: -1L,
            memberSplitInputs = it.members.map { m -> MemberSplitInput(m) }
        )
    }

    fun hideAddExpenseSheet() = _uiState.update { it.copy(showAddExpenseSheet = false) }
    fun onExpenseTitleChange(v: String) = _uiState.update { it.copy(expenseTitle = v, expenseTitleError = null) }
    fun onExpenseAmountChange(v: String) = _uiState.update { it.copy(expenseAmount = v, expenseAmountError = null) }
    fun onExpensePaidByChange(id: Long) = _uiState.update { it.copy(expensePaidBy = id) }
    fun onExpenseCategoryChange(c: Category) = _uiState.update { it.copy(expenseCategory = c) }
    fun onSplitTypeChange(type: SplitType) = _uiState.update { it.copy(expenseSplitType = type, splitInputError = null) }
    fun onMemberSplitInputChange(memberId: Long, value: String) = _uiState.update { state ->
        state.copy(
            memberSplitInputs = state.memberSplitInputs.map { if (it.member.id == memberId) it.copy(value = value) else it },
            splitInputError = null
        )
    }

    fun saveExpense() {
        val state = _uiState.value
        var hasError = false
        if (state.expenseTitle.isBlank()) { _uiState.update { it.copy(expenseTitleError = "Required") }; hasError = true }
        val amount = state.expenseAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) { _uiState.update { it.copy(expenseAmountError = "Enter valid amount") }; hasError = true }
        if (hasError) return

        // Compute per-member shares
        val shares: Map<Long, Double> = when (state.expenseSplitType) {
            SplitType.EQUAL -> state.members.associate { it.id to amount!! / state.members.size }
            SplitType.CUSTOM -> {
                val vals = state.memberSplitInputs.associate { it.member.id to (it.value.toDoubleOrNull() ?: 0.0) }
                if (kotlin.math.abs(vals.values.sum() - amount!!) > 0.01) {
                    _uiState.update { it.copy(splitInputError = "Amounts must sum to ${amount}") }; return
                }
                vals
            }
            SplitType.PERCENTAGE -> {
                val pcts = state.memberSplitInputs.associate { it.member.id to (it.value.toDoubleOrNull() ?: 0.0) }
                if (kotlin.math.abs(pcts.values.sum() - 100.0) > 0.01) {
                    _uiState.update { it.copy(splitInputError = "Percentages must sum to 100%") }; return
                }
                pcts.mapValues { (_, pct) -> amount!! * pct / 100.0 }
            }
            SplitType.SHARES -> {
                val shareNums = state.memberSplitInputs.associate { it.member.id to (it.value.toDoubleOrNull() ?: 1.0) }
                val totalShares = shareNums.values.sum()
                if (totalShares <= 0) { _uiState.update { it.copy(splitInputError = "Shares must be > 0") }; return }
                shareNums.mapValues { (_, s) -> amount!! * s / totalShares }
            }
        }

        viewModelScope.launch {
            val expenseId = splitRepository.insertExpense(SplitExpense(
                groupId = _groupId.value, title = state.expenseTitle.trim(),
                totalAmount = amount!!, paidByMemberId = state.expensePaidBy,
                date = LocalDate.now(), category = state.expenseCategory,
                splitType = state.expenseSplitType
            ))
            splitRepository.insertShares(state.members.map { member ->
                SplitShare(expenseId = expenseId, memberId = member.id, shareAmount = shares[member.id] ?: 0.0)
            })

            // Mirror into main expense tracker — only user's own share
            val me = state.members.find { it.isCurrentUser }
            if (me != null) {
                val myShare = shares[me.id] ?: 0.0
                expenseRepository.insertTransaction(Transaction(
                    title = if (state.expensePaidBy == me.id) "[Split] ${state.expenseTitle.trim()}" else "[Split Debt] ${state.expenseTitle.trim()}",
                    amount = myShare, type = TransactionType.EXPENSE,
                    category = state.expenseCategory, date = LocalDate.now(),
                    note = "Split expense · ${state.expenseSplitType.name.lowercase()}"
                ))
            }
            _uiState.update { it.copy(showAddExpenseSheet = false) }
        }
    }

    // ── Settle Up ─────────────────────────────────────────────────────────────
    fun showSettleSheet(targetMember: SplitMember) {
        val me = _uiState.value.memberBalances.find { it.member.isCurrentUser }
        val netOwedToTarget = if (me != null && me.net < -0.01) -me.net else 0.0
        _uiState.update {
            it.copy(
                showSettleSheet = true, settleTargetMember = targetMember,
                settleAmount = if (netOwedToTarget > 0) "%.2f".format(netOwedToTarget) else "",
                settleNote = "", settleAmountError = null, isPartialSettle = false
            )
        }
    }
    fun hideSettleSheet() = _uiState.update { it.copy(showSettleSheet = false) }
    fun onSettleAmountChange(v: String) = _uiState.update { it.copy(settleAmount = v, settleAmountError = null) }
    fun onSettleNoteChange(v: String) = _uiState.update { it.copy(settleNote = v) }
    fun onPartialSettleToggle() = _uiState.update { it.copy(isPartialSettle = !it.isPartialSettle) }

    fun confirmSettle() {
        val state = _uiState.value
        val target = state.settleTargetMember ?: return
        val amount = state.settleAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) { _uiState.update { it.copy(settleAmountError = "Enter valid amount") }; return }
        val me = state.members.find { it.isCurrentUser } ?: return

        viewModelScope.launch {
            // Record settlement
            splitRepository.insertSettlement(Settlement(
                groupId = _groupId.value, fromMemberId = me.id, toMemberId = target.id,
                amount = amount, note = state.settleNote.trim(),
                date = LocalDate.now(), isPartial = state.isPartialSettle
            ))
            // Record as expense in main tracker (paying off a debt)
            expenseRepository.insertTransaction(Transaction(
                title = "Settled with ${target.name}",
                amount = amount, type = TransactionType.EXPENSE,
                category = Category.OTHER, date = LocalDate.now(),
                note = "Split settlement${if (state.settleNote.isNotBlank()) ": ${state.settleNote}" else ""}"
            ))
            _uiState.update { it.copy(showSettleSheet = false) }
        }
    }

    fun deleteExpense(expense: SplitExpense) = viewModelScope.launch { splitRepository.deleteExpense(expense) }
}
