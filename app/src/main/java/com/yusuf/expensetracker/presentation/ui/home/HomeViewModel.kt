package com.yusuf.expensetracker.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensetracker.data.repository.ExpenseRepository
import com.yusuf.expensetracker.domain.model.Transaction
import com.yusuf.expensetracker.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val recentTransactions: List<Transaction> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val now = LocalDate.now()
        viewModelScope.launch {
            combine(
                repository.getAllTransactions(),
                repository.getTransactionsByMonth(now.year, now.monthValue)
            ) { all, monthly ->
                val income = monthly.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = monthly.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val balance = all.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } -
                        all.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                HomeUiState(
                    recentTransactions = all.take(5),
                    monthlyIncome = income,
                    monthlyExpense = expense,
                    totalBalance = balance,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
