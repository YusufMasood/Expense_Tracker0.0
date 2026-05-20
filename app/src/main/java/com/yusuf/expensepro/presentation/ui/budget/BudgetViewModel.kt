package com.yusuf.expensepro.presentation.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.ExpenseRepository
import com.yusuf.expensepro.domain.model.Budget
import com.yusuf.expensepro.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BudgetWithSpend(
    val budget: Budget,
    val spent: Double,
    val progress: Float,
    val isOverBudget: Boolean
)

data class BudgetUiState(
    val budgetsWithSpend: List<BudgetWithSpend> = emptyList(),
    val showAddDialog: Boolean = false,
    val dialogCategory: Category = Category.FOOD,
    val dialogAmount: String = "",
    val dialogAmountError: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val now = LocalDate.now()

    init {
        viewModelScope.launch {
            repository.getBudgetsForMonth(now.monthValue, now.year)
                .collect { budgets ->
                    val withSpend = budgets.map { budget ->
                        val spent = repository.getSpentForCategoryMonth(
                            budget.category, now.year, now.monthValue
                        ).first()
                        val progress = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat() else 0f
                        BudgetWithSpend(
                            budget = budget,
                            spent = spent,
                            progress = progress,
                            isOverBudget = spent > budget.limitAmount
                        )
                    }
                    _uiState.update { it.copy(budgetsWithSpend = withSpend, isLoading = false) }
                }
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true, dialogAmount = "", dialogAmountError = null) }
    fun hideDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun onDialogCategoryChange(c: Category) = _uiState.update { it.copy(dialogCategory = c) }
    fun onDialogAmountChange(v: String) = _uiState.update { it.copy(dialogAmount = v, dialogAmountError = null) }

    fun saveBudget() {
        val state = _uiState.value
        val amount = state.dialogAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(dialogAmountError = "Enter a valid amount") }
            return
        }
        viewModelScope.launch {
            repository.insertBudget(
                Budget(
                    category = state.dialogCategory,
                    limitAmount = amount,
                    month = now.monthValue,
                    year = now.year
                )
            )
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch { repository.deleteBudgetById(id) }
    }
}
