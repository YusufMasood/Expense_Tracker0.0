package com.yusuf.expensepro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yusuf.expensepro.domain.model.Category
import com.yusuf.expensepro.domain.model.Transaction
import com.yusuf.expensepro.domain.model.TransactionType
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String,       // TransactionType.name
    val category: String,   // Category.name
    val date: String,       // ISO LocalDate string
    val note: String
) {
    fun toDomain() = Transaction(
        id = id,
        title = title,
        amount = amount,
        type = TransactionType.valueOf(type),
        category = Category.valueOf(category),
        date = LocalDate.parse(date),
        note = note
    )
}

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    title = title,
    amount = amount,
    type = type.name,
    category = category.name,
    date = date.toString(),
    note = note
)
