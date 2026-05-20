package com.yusuf.expensepro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yusuf.expensepro.domain.model.Budget
import com.yusuf.expensepro.domain.model.Category

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val limitAmount: Double,
    val month: Int,
    val year: Int
) {
    fun toDomain() = Budget(
        id = id,
        category = Category.valueOf(category),
        limitAmount = limitAmount,
        month = month,
        year = year
    )
}

fun Budget.toEntity() = BudgetEntity(
    id = id,
    category = category.name,
    limitAmount = limitAmount,
    month = month,
    year = year
)
