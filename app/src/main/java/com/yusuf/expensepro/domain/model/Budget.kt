package com.yusuf.expensepro.domain.model

data class Budget(
    val id: Long = 0,
    val category: Category,
    val limitAmount: Double,
    val month: Int,  // 1-12
    val year: Int
)
