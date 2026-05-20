package com.yusuf.expensepro.presentation.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.ExpenseRepository
import com.yusuf.expensepro.domain.model.Transaction
import com.yusuf.expensepro.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TransactionFilter { ALL, INCOME, EXPENSE }

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val filter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionFilter.ALL)
    private val _search = MutableStateFlow("")
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.getAllTransactions(), _filter, _search) { all, filter, query ->
                val filtered = all
                    .filter { tx ->
                        when (filter) {
                            TransactionFilter.ALL     -> true
                            TransactionFilter.INCOME  -> tx.type == TransactionType.INCOME
                            TransactionFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
                        }
                    }
                    .filter { tx ->
                        query.isBlank() ||
                        tx.title.contains(query, ignoreCase = true) ||
                        tx.category.label.contains(query, ignoreCase = true) ||
                        tx.note.contains(query, ignoreCase = true)
                    }
                TransactionsUiState(transactions = filtered, filter = filter, searchQuery = query, isLoading = false)
            }.collect { _uiState.value = it }
        }
    }

    fun setFilter(filter: TransactionFilter) { _filter.value = filter }
    fun onSearchChange(query: String) { _search.value = query }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}
