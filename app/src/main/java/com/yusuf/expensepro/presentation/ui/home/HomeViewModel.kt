package com.yusuf.expensepro.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.ExpenseRepository
import com.yusuf.expensepro.data.repository.SplitRepository
import com.yusuf.expensepro.domain.model.Transaction
import com.yusuf.expensepro.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val recentTransactions: List<Transaction> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    // Pure transaction balance (income - expense)
    val transactionBalance: Double = 0.0,
    // Net from splits: positive = I get money, negative = I owe
    val splitNetBalance: Double = 0.0,
    // Total = transactionBalance + splitNetBalance
    val totalBalance: Double = 0.0,
    val splitToReceive: Double = 0.0,
    val splitOwed: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val splitRepository: SplitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        val now = LocalDate.now()
        viewModelScope.launch {
            combine(
                repository.getAllTransactions(),
                repository.getTransactionsByMonth(now.year, now.monthValue),
                splitRepository.getSplitSummary()
            ) { all, monthly, splitSummary ->
                val income  = monthly.filter { it.type == TransactionType.INCOME  }.sumOf { it.amount }
                val expense = monthly.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val txBalance = all.filter { it.type == TransactionType.INCOME  }.sumOf { it.amount } -
                               all.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                // Split net: money to receive is +, money owed is -
                val splitNet = splitSummary.totalToReceive - splitSummary.totalOwed
                HomeUiState(
                    recentTransactions = all.take(5),
                    monthlyIncome = income,
                    monthlyExpense = expense,
                    transactionBalance = txBalance,
                    splitNetBalance = splitNet,
                    totalBalance = txBalance + splitNet,   // reflects receivables
                    splitToReceive = splitSummary.totalToReceive,
                    splitOwed = splitSummary.totalOwed,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}
