package com.yusuf.expensepro.data.repository

import com.yusuf.expensepro.data.local.dao.BudgetDao
import com.yusuf.expensepro.data.local.dao.TransactionDao
import com.yusuf.expensepro.data.local.entity.toEntity
import com.yusuf.expensepro.domain.model.Budget
import com.yusuf.expensepro.domain.model.Category
import com.yusuf.expensepro.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ExpenseRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByMonth(year: Int, month: Int): Flow<List<Transaction>>
    fun getSpentForCategoryMonth(category: Category, year: Int, month: Int): Flow<Double>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteTransactionById(id: Long)

    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>
    suspend fun insertBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudgetById(id: Long)
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) : ExpenseRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByMonth(year: Int, month: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsByMonth(
            year.toString(),
            month.toString().padStart(2, '0')
        ).map { list -> list.map { it.toDomain() } }

    override fun getSpentForCategoryMonth(category: Category, year: Int, month: Int): Flow<Double> =
        transactionDao.getSpentForCategoryMonth(
            category.name,
            year.toString(),
            month.toString().padStart(2, '0')
        ).map { it ?: 0.0 }

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)?.toDomain()

    override suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction.toEntity())

    override suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)

    override fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(month, year).map { list -> list.map { it.toDomain() } }

    override suspend fun insertBudget(budget: Budget): Long =
        budgetDao.insertBudget(budget.toEntity())

    override suspend fun updateBudget(budget: Budget) =
        budgetDao.updateBudget(budget.toEntity())

    override suspend fun deleteBudgetById(id: Long) =
        budgetDao.deleteBudgetById(id)
}
