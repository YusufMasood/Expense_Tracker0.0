package com.yusuf.expensepro.data.repository

import com.yusuf.expensepro.data.local.dao.*
import com.yusuf.expensepro.data.local.entity.*
import com.yusuf.expensepro.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SplitRepository {
    fun getAllGroups(): Flow<List<SplitGroup>>
    suspend fun insertGroup(group: SplitGroup): Long
    suspend fun deleteGroup(group: SplitGroup)
    suspend fun getGroupById(id: Long): SplitGroup?

    fun getMembersForGroup(groupId: Long): Flow<List<SplitMember>>
    suspend fun getMembersForGroupSync(groupId: Long): List<SplitMember>
    suspend fun insertMember(member: SplitMember): Long
    suspend fun deleteMember(member: SplitMember)

    fun getExpensesForGroup(groupId: Long): Flow<List<SplitExpense>>
    suspend fun insertExpense(expense: SplitExpense): Long
    suspend fun deleteExpense(expense: SplitExpense)
    fun getAllExpenses(): Flow<List<SplitExpense>>

    fun getSharesForExpense(expenseId: Long): Flow<List<SplitShare>>
    suspend fun getSharesForExpenseSync(expenseId: Long): List<SplitShare>
    suspend fun insertShares(shares: List<SplitShare>)

    fun getSettlementsForGroup(groupId: Long): Flow<List<Settlement>>
    suspend fun insertSettlement(settlement: Settlement): Long
    suspend fun deleteSettlement(settlement: Settlement)

    suspend fun getMemberBalances(groupId: Long): List<MemberBalance>
    fun getSplitSummary(): Flow<SplitSummary>
}

@Singleton
class SplitRepositoryImpl @Inject constructor(
    private val groupDao: SplitGroupDao,
    private val memberDao: SplitMemberDao,
    private val expenseDao: SplitExpenseDao,
    private val shareDao: SplitShareDao,
    private val settlementDao: SettlementDao
) : SplitRepository {

    override fun getAllGroups() =
        groupDao.getAllGroups().map { it.map { e -> e.toDomain() } }

    override suspend fun insertGroup(group: SplitGroup) =
        groupDao.insertGroup(group.toEntity())

    override suspend fun deleteGroup(group: SplitGroup) =
        groupDao.deleteGroup(group.toEntity())

    override suspend fun getGroupById(id: Long) =
        groupDao.getGroupById(id)?.toDomain()

    override fun getMembersForGroup(groupId: Long) =
        memberDao.getMembersForGroup(groupId).map { it.map { e -> e.toDomain() } }

    override suspend fun getMembersForGroupSync(groupId: Long) =
        memberDao.getMembersForGroupSync(groupId).map { it.toDomain() }

    override suspend fun insertMember(member: SplitMember) =
        memberDao.insertMember(member.toEntity())

    override suspend fun deleteMember(member: SplitMember) =
        memberDao.deleteMember(member.toEntity())

    override fun getExpensesForGroup(groupId: Long) =
        expenseDao.getExpensesForGroup(groupId).map { it.map { e -> e.toDomain() } }

    override suspend fun insertExpense(expense: SplitExpense) =
        expenseDao.insertExpense(expense.toEntity())

    override suspend fun deleteExpense(expense: SplitExpense) =
        expenseDao.deleteExpense(expense.toEntity())

    override fun getAllExpenses() =
        expenseDao.getAllExpenses().map { it.map { e -> e.toDomain() } }

    override fun getSharesForExpense(expenseId: Long) =
        shareDao.getSharesForExpense(expenseId).map { it.map { e -> e.toDomain() } }

    override suspend fun getSharesForExpenseSync(expenseId: Long) =
        shareDao.getSharesForExpenseSync(expenseId).map { it.toDomain() }

    override suspend fun insertShares(shares: List<SplitShare>) =
        shareDao.insertShares(shares.map { it.toEntity() })

    override fun getSettlementsForGroup(groupId: Long) =
        settlementDao.getSettlementsForGroup(groupId).map { it.map { e -> e.toDomain() } }

    override suspend fun insertSettlement(settlement: Settlement) =
        settlementDao.insertSettlement(settlement.toEntity())

    override suspend fun deleteSettlement(settlement: Settlement) =
        settlementDao.deleteSettlement(settlement.toEntity())

    /**
     * Calculates net balance for every member in a group.
     *
     * For each member:
     *   totalPaid = sum of totalAmount for all expenses where paidByMemberId == member.id
     *   totalOwed = sum of shareAmount across all their SplitShare rows (their portion of every expense)
     *   net       = totalPaid - totalOwed
     *               > 0  → they should receive money
     *               < 0  → they owe money
     *
     * Settlements shift net: fromMember paid toMember → fromMember's net improves by amount,
     * toMember's net reduces by amount (they received what was owed).
     */
    override suspend fun getMemberBalances(groupId: Long): List<MemberBalance> {
        val members = memberDao.getMembersForGroupSync(groupId).map { it.toDomain() }
        val expenses = expenseDao.getExpensesForGroup(groupId).first()

        // Build maps: memberId → accumulated paid & owed
        val paid = members.associate { it.id to 0.0 }.toMutableMap()
        val owed = members.associate { it.id to 0.0 }.toMutableMap()

        expenses.forEach { expense ->
            // Who paid the full bill
            paid[expense.paidByMemberId] =
                (paid[expense.paidByMemberId] ?: 0.0) + expense.totalAmount

            // Each member owes their share
            val shares = shareDao.getSharesForExpenseSync(expense.id)
            shares.forEach { share ->
                owed[share.memberId] = (owed[share.memberId] ?: 0.0) + share.shareAmount
            }
        }

        // Factor in settlements
        val settlements = settlementDao.getSettlementsForGroup(groupId).first()
        val settlementAdjust = members.associate { it.id to 0.0 }.toMutableMap()
        settlements.forEach { s ->
            // fromMember paid toMember: fromMember's debt reduces, toMember's receivable reduces
            settlementAdjust[s.fromMemberId] =
                (settlementAdjust[s.fromMemberId] ?: 0.0) + s.amount
            settlementAdjust[s.toMemberId] =
                (settlementAdjust[s.toMemberId] ?: 0.0) - s.amount
        }

        return members.map { member ->
            val p   = paid[member.id] ?: 0.0
            val o   = owed[member.id] ?: 0.0
            val adj = settlementAdjust[member.id] ?: 0.0
            MemberBalance(
                member     = member,
                totalPaid  = p,
                totalOwed  = o,
                net        = (p - o) + adj
            )
        }
    }

    override fun getSplitSummary(): Flow<SplitSummary> =
        getAllGroups().map { groups ->
            var toReceive = 0.0
            var owes      = 0.0
            groups.forEach { group ->
                val balances = getMemberBalances(group.id)
                val me = balances.find { it.member.isCurrentUser } ?: return@forEach
                if (me.net > 0.01) toReceive += me.net
                else if (me.net < -0.01) owes += -me.net
            }
            SplitSummary(toReceive, owes)
        }
}
