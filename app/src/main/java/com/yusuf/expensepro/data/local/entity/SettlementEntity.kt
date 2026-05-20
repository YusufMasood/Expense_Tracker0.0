package com.yusuf.expensepro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yusuf.expensepro.domain.model.Settlement
import java.time.LocalDate

@Entity(
    tableName = "settlements",
    foreignKeys = [ForeignKey(
        entity = SplitGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("groupId")]
)
data class SettlementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val fromMemberId: Long,
    val toMemberId: Long,
    val amount: Double,
    val note: String,
    val date: String,
    val isPartial: Boolean
) {
    fun toDomain() = Settlement(id, groupId, fromMemberId, toMemberId, amount, note, LocalDate.parse(date), isPartial)
}

fun Settlement.toEntity() = SettlementEntity(id, groupId, fromMemberId, toMemberId, amount, note, date.toString(), isPartial)
