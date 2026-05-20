package com.yusuf.expensepro.presentation.ui.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.ExpenseRepository
import com.yusuf.expensepro.domain.model.Category
import com.yusuf.expensepro.domain.model.Transaction
import com.yusuf.expensepro.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditUiState(
    val title: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: Category = Category.OTHER,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val titleError: String? = null,
    val amountError: String? = null
)

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private var editingId: Long? = null

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(id) ?: return@launch
            editingId = id
            _uiState.update {
                it.copy(
                    title = transaction.title,
                    amount = transaction.amount.toString(),
                    type = transaction.type,
                    category = transaction.category,
                    date = transaction.date,
                    note = transaction.note,
                    isEditMode = true
                )
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, titleError = null) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amount = value, amountError = null) }
    fun onTypeChange(value: TransactionType) = _uiState.update { it.copy(type = value) }
    fun onCategoryChange(value: Category) = _uiState.update { it.copy(category = value) }
    fun onDateChange(value: LocalDate) = _uiState.update { it.copy(date = value) }
    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }

    fun save() {
        val state = _uiState.value
        var hasError = false

        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            hasError = true
        }
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(amountError = "Enter a valid amount") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            val transaction = Transaction(
                id = editingId ?: 0,
                title = state.title.trim(),
                amount = amount!!,
                type = state.type,
                category = state.category,
                date = state.date,
                note = state.note.trim()
            )
            if (state.isEditMode) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
