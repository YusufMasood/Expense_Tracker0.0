package com.yusuf.expensepro.presentation.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusuf.expensepro.data.repository.ExpenseRepository
import com.yusuf.expensepro.data.repository.SplitRepository
import com.yusuf.expensepro.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import javax.inject.Inject

data class CategorySpend(val category: Category, val amount: Double, val percentage: Float)
data class WeeklyPoint(val label: String, val amount: Double)  // "Mon", "Tue" etc

data class StatsUiState(
    val selectedMonth: LocalDate = LocalDate.now(),
    val monthlyTransactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val weeklyTrend: List<WeeklyPoint> = emptyList(),
    val splitToReceive: Double = 0.0,
    val splitOwed: Double = 0.0,
    val avgDailySpend: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val splitRepository: SplitRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(LocalDate.now())
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _selectedMonth.flatMapLatest { month ->
                    repository.getTransactionsByMonth(month.year, month.monthValue)
                },
                splitRepository.getSplitSummary()
            ) { transactions, splitSummary ->
                val month = _selectedMonth.value
                val income  = transactions.filter { it.type == TransactionType.INCOME  }.sumOf { it.amount }
                val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                val byCategory = transactions.filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category }
                    .map { (cat, list) ->
                        val total = list.sumOf { it.amount }
                        CategorySpend(cat, total, if (expense > 0) (total / expense).toFloat() else 0f)
                    }.sortedByDescending { it.amount }

                // Weekly trend — last 7 days of selected month
                val today = LocalDate.now()
                val endDate = if (month.year == today.year && month.monthValue == today.monthValue) today
                              else month.withDayOfMonth(month.lengthOfMonth())
                val startDate = endDate.minusDays(6)
                val weeklyPoints = (0..6).map { offset ->
                    val day = startDate.plusDays(offset.toLong())
                    val dayTotal = transactions
                        .filter { it.type == TransactionType.EXPENSE && it.date == day }
                        .sumOf { it.amount }
                    WeeklyPoint(
                        label = day.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                        amount = dayTotal
                    )
                }

                val daysInMonth = month.lengthOfMonth()
                val avgDaily = if (daysInMonth > 0) expense / daysInMonth else 0.0

                StatsUiState(
                    selectedMonth = month,
                    monthlyTransactions = transactions,
                    totalIncome = income,
                    totalExpense = expense,
                    categoryBreakdown = byCategory,
                    weeklyTrend = weeklyPoints,
                    splitToReceive = splitSummary.totalToReceive,
                    splitOwed = splitSummary.totalOwed,
                    avgDailySpend = avgDaily,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun previousMonth() = _selectedMonth.update { it.minusMonths(1) }
    fun nextMonth() {
        val next = _selectedMonth.value.plusMonths(1)
        if (!next.isAfter(LocalDate.now())) _selectedMonth.update { next }
    }
}
