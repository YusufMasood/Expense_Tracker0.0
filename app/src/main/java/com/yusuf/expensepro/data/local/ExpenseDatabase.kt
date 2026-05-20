package com.yusuf.expensepro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yusuf.expensepro.data.local.dao.*
import com.yusuf.expensepro.data.local.entity.*

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        SplitGroupEntity::class,
        SplitMemberEntity::class,
        SplitExpenseEntity::class,
        SplitShareEntity::class,
        SettlementEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun splitGroupDao(): SplitGroupDao
    abstract fun splitMemberDao(): SplitMemberDao
    abstract fun splitExpenseDao(): SplitExpenseDao
    abstract fun splitShareDao(): SplitShareDao
    abstract fun settlementDao(): SettlementDao
}
