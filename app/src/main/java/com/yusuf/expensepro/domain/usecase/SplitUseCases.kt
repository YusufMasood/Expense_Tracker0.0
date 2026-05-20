package com.yusuf.expensepro.domain.usecase

import com.yusuf.expensepro.data.repository.SplitRepository
import com.yusuf.expensepro.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

// ── Get All Groups ────────────────────────────────────────────────────────────

class GetGroupsUseCase @Inject constructor(
    private val repository: SplitRepository
) {
    operator fun invoke(): Flow<List<SplitGroup>> =
        repository.getAllGroups()
}

// ── Create Group ──────────────────────────────────────────────────────────────

class CreateGroupUseCase @Inject constructor(
    private val repository: SplitRepository
) {
    /**
     * Creates a new split group and auto-adds the current user as "Me".
     * Returns the new group's ID.
     */
    suspend operator fun invoke(name: String, icon: String = "👥"): Result<Long> {
        return try {
            require(name.isNotBlank()) { "Group name cannot be empty" }
            val groupId = repository.insertGroup(
                SplitGroup(name = name.trim(), icon = icon, createdAt = LocalDate.now())
            )
            // Auto-add "Me" as the first member
            repository.insertMember(
                SplitMember(groupId = groupId, name = "Me", isCurrentUser = true)
            )
            Result.success(groupId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ── Add Split Expense ─────────────────────────────────────────────────────────

class AddSplitExpenseUseCase @Inject constructor(
    private val repository: SplitRepository
) {
    /**
     * Validates and inserts a split expense along with per-member shares.
     *
     * @param expense  The expense to insert
     * @param members  All members in the group
     * @param splitType How to split (EQUAL, CUSTOM, PERCENTAGE, SHARES)
     * @param customValues  For non-EQUAL splits: map of memberId → raw value
     */
    suspend operator fun invoke(
        expense: SplitExpense,
        members: List<SplitMember>,
        splitType: SplitType,
        customValues: Map<Long, Double> = emptyMap()
    ): Result<Long> {
        return try {
            require(expense.title.isNotBlank()) { "Expense title cannot be empty" }
            require(expense.totalAmount > 0) { "Amount must be positive" }
            require(members.isNotEmpty()) { "Group must have at least one member" }

            val shares = calculateShares(expense.totalAmount, members, splitType, customValues)

            val expenseId = repository.insertExpense(expense.copy(splitType = splitType))
            repository.insertShares(members.map { member ->
                SplitShare(
                    expenseId   = expenseId,
                    memberId    = member.id,
                    shareAmount = shares[member.id] ?: 0.0
                )
            })
            Result.success(expenseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pure function — calculates how much each member owes.
     * Kept in domain layer because it's core business logic.
     */
    fun calculateShares(
        totalAmount: Double,
        members: List<SplitMember>,
        splitType: SplitType,
        customValues: Map<Long, Double>
    ): Map<Long, Double> = when (splitType) {
        SplitType.EQUAL -> {
            val shareAmount = totalAmount / members.size
            members.associate { it.id to shareAmount }
        }
        SplitType.CUSTOM -> {
            val total = customValues.values.sum()
            require(kotlin.math.abs(total - totalAmount) < 0.01) {
                "Custom amounts must sum to $totalAmount (got $total)"
            }
            customValues
        }
        SplitType.PERCENTAGE -> {
            val total = customValues.values.sum()
            require(kotlin.math.abs(total - 100.0) < 0.01) {
                "Percentages must sum to 100% (got $total)"
            }
            customValues.mapValues { (_, pct) -> totalAmount * pct / 100.0 }
        }
        SplitType.SHARES -> {
            val totalShares = customValues.values.sum()
            require(totalShares > 0) { "Total shares must be > 0" }
            customValues.mapValues { (_, shares) -> totalAmount * shares / totalShares }
        }
    }
}

// ── Settle Up ─────────────────────────────────────────────────────────────────

class SettleUpUseCase @Inject constructor(
    private val repository: SplitRepository
) {
    suspend operator fun invoke(
        groupId: Long,
        fromMemberId: Long,
        toMemberId: Long,
        amount: Double,
        note: String = "",
        isPartial: Boolean = false
    ): Result<Long> {
        return try {
            require(amount > 0) { "Settlement amount must be positive" }
            require(fromMemberId != toMemberId) { "Cannot settle with yourself" }

            val settlementId = repository.insertSettlement(
                Settlement(
                    groupId      = groupId,
                    fromMemberId = fromMemberId,
                    toMemberId   = toMemberId,
                    amount       = amount,
                    note         = note.trim(),
                    date         = LocalDate.now(),
                    isPartial    = isPartial
                )
            )
            Result.success(settlementId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ── Get Member Balances ───────────────────────────────────────────────────────

class GetMemberBalancesUseCase @Inject constructor(
    private val repository: SplitRepository
) {
    suspend operator fun invoke(groupId: Long): List<MemberBalance> =
        repository.getMemberBalances(groupId)
}
