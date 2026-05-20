package com.yusuf.expensepro.domain.usecase

import com.yusuf.expensepro.data.repository.ExpenseRepository
import com.yusuf.expensepro.domain.model.Category
import com.yusuf.expensepro.domain.model.Transaction
import com.yusuf.expensepro.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use cases encapsulate single business operations.
 * This layer sits between ViewModel and Repository.
 *
 * Benefits:
 *  - Each use case is independently testable
 *  - Business logic is NOT in ViewModel or Repository
 *  - Easy to swap data sources later (Room → Retrofit)
 */

// ── Get All Transactions ─────────────────────────────────────────────────────

class GetAllTransactionsUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Transaction>> =
        repository.getAllTransactions()
}

// ── Get Monthly Transactions ─────────────────────────────────────────────────

class GetMonthlyTransactionsUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(year: Int, month: Int): Flow<List<Transaction>> =
        repository.getTransactionsByMonth(year, month)
}

// ── Add Transaction ──────────────────────────────────────────────────────────

class AddTransactionUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    /**
     * Validates and inserts a transaction.
     * Returns Result.success(id) or Result.failure(exception).
     */
    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        return try {
            validateTransaction(transaction)
            val id = repository.insertTransaction(transaction)
            Result.success(id)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    private fun validateTransaction(transaction: Transaction) {
        require(transaction.title.isNotBlank()) { "Title cannot be empty" }
        require(transaction.amount > 0) { "Amount must be positive" }
        require(!transaction.date.isAfter(LocalDate.now())) { "Date cannot be in the future" }
    }
}

// ── Update Transaction ───────────────────────────────────────────────────────

class UpdateTransactionUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return try {
            require(transaction.id > 0) { "Invalid transaction ID" }
            require(transaction.title.isNotBlank()) { "Title cannot be empty" }
            require(transaction.amount > 0) { "Amount must be positive" }
            repository.updateTransaction(transaction)
            Result.success(Unit)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }
}

// ── Delete Transaction ───────────────────────────────────────────────────────

class DeleteTransactionUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(transaction: Transaction) =
        repository.deleteTransaction(transaction)

    suspend operator fun invoke(id: Long) =
        repository.deleteTransactionById(id)
}

// ── Get Transaction By ID ────────────────────────────────────────────────────

class GetTransactionByIdUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(id: Long): Transaction? =
        repository.getTransactionById(id)
}

// ── Calculate Monthly Summary ────────────────────────────────────────────────

data class MonthlySummary(
    val income: Double,
    val expense: Double,
    val net: Double,
    val topCategory: Category?,
    val transactionCount: Int
)

class GetMonthlySummaryUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    /**
     * Calculates a summary for the given month.
     * This is pure business logic — belongs in domain, not ViewModel.
     */
    operator fun invoke(year: Int, month: Int): Flow<MonthlySummary> =
        repository.getTransactionsByMonth(year, month).map { transactions ->
            val income  = transactions.filter { it.type == TransactionType.INCOME  }.sumOf { it.amount }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val topCategory = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .maxByOrNull { it.value.sumOf { tx -> tx.amount } }
                ?.key
            MonthlySummary(
                income           = income,
                expense          = expense,
                net              = income - expense,
                topCategory      = topCategory,
                transactionCount = transactions.size
            )
        }
}
