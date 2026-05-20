package com.yusuf.expensepro.domain.model

import java.time.LocalDate

enum class TransactionType { INCOME, EXPENSE }

enum class Category(val label: String, val icon: String) {
    FOOD("Food & Dining", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍️"),
    ENTERTAINMENT("Entertainment", "🎮"),
    HEALTH("Health", "🏥"),
    EDUCATION("Education", "📚"),
    UTILITIES("Utilities", "💡"),
    RENT("Rent", "🏠"),
    SALARY("Salary", "💼"),
    FREELANCE("Freelance", "💻"),
    INVESTMENT("Investment", "📈"),
    OTHER("Other", "💰")
}

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val date: LocalDate,
    val note: String = ""
)
