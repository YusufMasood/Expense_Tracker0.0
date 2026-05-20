package com.yusuf.expensepro.data.local.dao

import androidx.room.*
import com.yusuf.expensepro.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitGroupDao {
    @Query("SELECT * FROM split_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<SplitGroupEntity>>

    @Query("SELECT * FROM split_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): SplitGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: SplitGroupEntity): Long

    @Delete
    suspend fun deleteGroup(group: SplitGroupEntity)
}

@Dao
interface SplitMemberDao {
    @Query("SELECT * FROM split_members WHERE groupId = :groupId")
    fun getMembersForGroup(groupId: Long): Flow<List<SplitMemberEntity>>

    @Query("SELECT * FROM split_members WHERE groupId = :groupId")
    suspend fun getMembersForGroupSync(groupId: Long): List<SplitMemberEntity>

    @Query("SELECT * FROM split_members WHERE id = :id")
    suspend fun getMemberById(id: Long): SplitMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: SplitMemberEntity): Long

    @Delete
    suspend fun deleteMember(member: SplitMemberEntity)
}

@Dao
interface SplitExpenseDao {
    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY date DESC")
    fun getExpensesForGroup(groupId: Long): Flow<List<SplitExpenseEntity>>

    @Query("SELECT * FROM split_expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): SplitExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: SplitExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: SplitExpenseEntity)

    @Query("SELECT * FROM split_expenses")
    fun getAllExpenses(): Flow<List<SplitExpenseEntity>>
}

@Dao
interface SplitShareDao {
    @Query("SELECT * FROM split_shares WHERE expenseId = :expenseId")
    fun getSharesForExpense(expenseId: Long): Flow<List<SplitShareEntity>>

    @Query("SELECT * FROM split_shares WHERE expenseId = :expenseId")
    suspend fun getSharesForExpenseSync(expenseId: Long): List<SplitShareEntity>

    @Query("SELECT * FROM split_shares WHERE memberId = :memberId")
    suspend fun getSharesForMember(memberId: Long): List<SplitShareEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShare(share: SplitShareEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShares(shares: List<SplitShareEntity>)

    @Query("UPDATE split_shares SET isSettled = 1 WHERE memberId = :memberId AND expenseId IN (SELECT id FROM split_expenses WHERE groupId = :groupId)")
    suspend fun settleAllForMemberInGroup(memberId: Long, groupId: Long)

    @Delete
    suspend fun deleteShare(share: SplitShareEntity)
}

@Dao
interface SettlementDao {
    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY date DESC")
    fun getSettlementsForGroup(groupId: Long): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements ORDER BY date DESC")
    fun getAllSettlements(): Flow<List<SettlementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity): Long

    @Delete
    suspend fun deleteSettlement(settlement: SettlementEntity)
}
