package com.yusuf.expensepro.data.local.dao

import androidx.room.*
import com.yusuf.expensepro.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("""
        SELECT * FROM transactions 
        WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month
        ORDER BY date DESC
    """)
    fun getTransactionsByMonth(year: String, month: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'EXPENSE' 
        AND category = :category
        AND strftime('%Y', date) = :year 
        AND strftime('%m', date) = :month
    """)
    fun getSpentForCategoryMonth(category: String, year: String, month: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}
