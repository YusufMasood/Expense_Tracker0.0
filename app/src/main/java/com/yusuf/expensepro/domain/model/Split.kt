package com.yusuf.expensepro.domain.model

import java.time.LocalDate

data class SplitGroup(
    val id: Long = 0,
    val name: String,
    val icon: String = "👥",
    val createdAt: LocalDate = LocalDate.now()
)

data class SplitMember(
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val isCurrentUser: Boolean = false
)

data class SplitExpense(
    val id: Long = 0,
    val groupId: Long,
    val title: String,
    val totalAmount: Double,
    val paidByMemberId: Long,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val category: Category = Category.OTHER,
    val splitType: SplitType = SplitType.EQUAL
)

data class SplitShare(
    val id: Long = 0,
    val expenseId: Long,
    val memberId: Long,
    val shareAmount: Double,
    val isSettled: Boolean = false
)

data class Settlement(
    val id: Long = 0,
    val groupId: Long,
    val fromMemberId: Long,  // who pays
    val toMemberId: Long,    // who receives
    val amount: Double,
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val isPartial: Boolean = false
)

enum class SplitType {
    EQUAL,      // divide equally
    CUSTOM,     // each person enters exact amount
    PERCENTAGE, // each person gets a % of total
    SHARES      // each person gets a number of shares
}

data class MemberBalance(
    val member: SplitMember,
    val totalPaid: Double,
    val totalOwed: Double,
    val net: Double   // positive = should receive, negative = owes
)

data class SplitSummary(
    val totalToReceive: Double,
    val totalOwed: Double
)

// Helper: per-member custom split input
data class MemberSplitInput(
    val member: SplitMember,
    val value: String = ""   // amount / percentage / shares depending on SplitType
)
