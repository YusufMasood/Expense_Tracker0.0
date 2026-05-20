package com.yusuf.expensepro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yusuf.expensepro.domain.model.*
import java.time.LocalDate

// ── SplitGroup ──────────────────────────────────────────────────────────────

@Entity(tableName = "split_groups")
data class SplitGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val createdAt: String
) {
    fun toDomain() = SplitGroup(id, name, icon, LocalDate.parse(createdAt))
}

fun SplitGroup.toEntity() = SplitGroupEntity(id, name, icon, createdAt.toString())

// ── SplitMember ─────────────────────────────────────────────────────────────

@Entity(
    tableName = "split_members",
    foreignKeys = [ForeignKey(
        entity = SplitGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("groupId")]
)
data class SplitMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val name: String,
    val isCurrentUser: Boolean
) {
    fun toDomain() = SplitMember(id, groupId, name, isCurrentUser)
}

fun SplitMember.toEntity() = SplitMemberEntity(id, groupId, name, isCurrentUser)

// ── SplitExpense ─────────────────────────────────────────────────────────────

@Entity(
    tableName = "split_expenses",
    foreignKeys = [ForeignKey(
        entity = SplitGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("groupId")]
)
data class SplitExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val title: String,
    val totalAmount: Double,
    val paidByMemberId: Long,
    val date: String,
    val note: String,
    val category: String,
    val splitType: String
) {
    fun toDomain() = SplitExpense(
        id, groupId, title, totalAmount, paidByMemberId,
        LocalDate.parse(date), note, Category.valueOf(category), SplitType.valueOf(splitType)
    )
}

fun SplitExpense.toEntity() = SplitExpenseEntity(
    id, groupId, title, totalAmount, paidByMemberId,
    date.toString(), note, category.name, splitType.name
)

// ── SplitShare ────────────────────────────────────────────────────────────────

@Entity(
    tableName = "split_shares",
    foreignKeys = [ForeignKey(
        entity = SplitExpenseEntity::class,
        parentColumns = ["id"],
        childColumns = ["expenseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("expenseId")]
)
data class SplitShareEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expenseId: Long,
    val memberId: Long,
    val shareAmount: Double,
    val isSettled: Boolean
) {
    fun toDomain() = SplitShare(id, expenseId, memberId, shareAmount, isSettled)
}

fun SplitShare.toEntity() = SplitShareEntity(id, expenseId, memberId, shareAmount, isSettled)
