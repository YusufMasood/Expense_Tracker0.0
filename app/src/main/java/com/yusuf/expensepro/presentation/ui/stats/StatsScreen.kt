package com.yusuf.expensepro.presentation.ui.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusuf.expensepro.presentation.theme.ExpenseRed
import com.yusuf.expensepro.presentation.theme.IncomeGreen
import com.yusuf.expensepro.presentation.ui.components.EmptyState
import com.yusuf.expensepro.util.formatAmount
import com.yusuf.expensepro.util.formatMonthYear

private val chartColors = listOf(
    Color(0xFF6E56F5), Color(0xFF00C853), Color(0xFFFFB300), Color(0xFF00BCD4),
    Color(0xFFE91E63), Color(0xFF8E24AA), Color(0xFF4CAF50), Color(0xFFFF5722)
)
private val SplitYellow = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit, viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val net = state.totalIncome - state.totalExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Month navigator ──────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = viewModel::previousMonth) { Icon(Icons.Default.ChevronLeft, null) }
                    Text(state.selectedMonth.formatMonthYear(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = viewModel::nextMonth) { Icon(Icons.Default.ChevronRight, null) }
                }
            }

            // ── Income / Expense summary cards ───────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Income", state.totalIncome, IncomeGreen, "↑", Modifier.weight(1f))
                    StatCard("Expenses", state.totalExpense, ExpenseRed, "↓", Modifier.weight(1f))
                }
            }

            // ── Net savings + avg daily ──────────────────────────────────────
            item {
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (net >= 0) IncomeGreen.copy(0.1f) else ExpenseRed.copy(0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Net Savings", style = MaterialTheme.typography.bodySmall, color = if (net >= 0) IncomeGreen.copy(0.8f) else ExpenseRed.copy(0.8f))
                            Spacer(Modifier.height(4.dp))
                            Text(net.formatAmount(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (net >= 0) IncomeGreen else ExpenseRed)
                            Text(if (net >= 0) "🎉 Surplus" else "⚠️ Deficit", style = MaterialTheme.typography.labelSmall, color = if (net >= 0) IncomeGreen.copy(0.7f) else ExpenseRed.copy(0.7f))
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6E56F5).copy(0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Avg Daily Spend", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6E56F5).copy(0.8f))
                            Spacer(Modifier.height(4.dp))
                            Text(state.avgDailySpend.formatAmount(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF6E56F5))
                            Text("per day", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6E56F5).copy(0.7f))
                        }
                    }
                }
            }

            // ── Donut chart ──────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(20.dp))
                Text("Expense Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
                if (state.categoryBreakdown.isNotEmpty()) {
                    DonutChart(
                        segments = state.categoryBreakdown.take(8).mapIndexed { i, s ->
                            DonutSegment(s.percentage, chartColors[i % chartColors.size], s.category.label)
                        },
                        centerText = state.totalExpense.formatAmount(),
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.categoryBreakdown.take(4).forEachIndexed { i, spend ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(chartColors[i]))
                                Spacer(Modifier.width(4.dp))
                                Text(spend.category.icon, fontSize = 10.sp)
                            }
                        }
                    }
                } else {
                    EmptyState("No expense data for this month.", modifier = Modifier.padding(24.dp))
                }
            }

            // ── Weekly spending bar chart ────────────────────────────────────
            if (state.weeklyTrend.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Last 7 Days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(12.dp))
                    WeeklyBarChart(
                        points = state.weeklyTrend,
                        modifier = Modifier.fillMaxWidth().height(140.dp).padding(horizontal = 16.dp)
                    )
                }
            }

            // ── Split analytics ──────────────────────────────────────────────
            if (state.splitToReceive > 0.01 || state.splitOwed > 0.01) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Split Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (state.splitToReceive > 0.01) {
                            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(0.1f))) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("↑ To Receive", style = MaterialTheme.typography.labelMedium, color = IncomeGreen.copy(0.8f))
                                    Spacer(Modifier.height(4.dp))
                                    Text(state.splitToReceive.formatAmount(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                    Text("pending from groups", style = MaterialTheme.typography.labelSmall, color = IncomeGreen.copy(0.6f))
                                }
                            }
                        }
                        if (state.splitOwed > 0.01) {
                            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SplitYellow.copy(0.1f))) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("↓ You Owe", style = MaterialTheme.typography.labelMedium, color = SplitYellow.copy(0.8f))
                                    Spacer(Modifier.height(4.dp))
                                    Text(state.splitOwed.formatAmount(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SplitYellow)
                                    Text("pending to others", style = MaterialTheme.typography.labelSmall, color = SplitYellow.copy(0.6f))
                                }
                            }
                        }
                    }
                }
            }

            // ── Category breakdown rows ──────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                Text("By Category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
            }

            if (state.categoryBreakdown.isEmpty()) {
                item { EmptyState("No data.", modifier = Modifier.padding(16.dp)) }
            } else {
                items(state.categoryBreakdown.take(8).withIndex().toList()) { (index, spend) ->
                    AnimatedCategoryRow(
                        icon = spend.category.icon, label = spend.category.label,
                        amount = spend.amount, percentage = spend.percentage,
                        color = chartColors[index % chartColors.size],
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

// ── Donut chart ───────────────────────────────────────────────────────────────
data class DonutSegment(val fraction: Float, val color: Color, val label: String)

@Composable
private fun DonutChart(segments: List<DonutSegment>, centerText: String, modifier: Modifier = Modifier) {
    val animProgress by animateFloatAsState(
        targetValue = 1f, animationSpec = tween(1400, easing = FastOutSlowInEasing), label = "donut"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 48f
            val stroke = Stroke(width = strokeW, cap = StrokeCap.Butt)
            val inset = strokeW / 2 + 4f
            val rect = Size(size.width - inset * 2, size.height - inset * 2)
            var startAngle = -90f
            // Background ring
            drawArc(Color(0xFF1A1A1A), 0f, 360f, false, Offset(inset, inset), rect, style = stroke)
            segments.forEach { seg ->
                val sweep = seg.fraction * 360f * animProgress
                if (sweep > 0.5f) drawArc(seg.color, startAngle, sweep, false, Offset(inset, inset), rect, style = stroke)
                startAngle += seg.fraction * 360f * animProgress
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Spent", color = Color(0xFF888888), fontSize = 11.sp)
            Text(centerText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ── Weekly bar chart ──────────────────────────────────────────────────────────
@Composable
private fun WeeklyBarChart(points: List<WeeklyPoint>, modifier: Modifier = Modifier) {
    val animProgress by animateFloatAsState(
        targetValue = 1f, animationSpec = tween(1000, easing = FastOutSlowInEasing), label = "bar"
    )
    val maxAmount = points.maxOfOrNull { it.amount } ?: 1.0

    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val barWidth = size.width / (points.size * 2f)
                val gap = barWidth
                points.forEachIndexed { index, point ->
                    val barHeight = if (maxAmount > 0) (point.amount / maxAmount * size.height * animProgress).toFloat() else 0f
                    val x = index * (barWidth + gap) + gap / 2
                    val y = size.height - barHeight
                    val color = if (point.amount == points.maxOfOrNull { it.amount }) Color(0xFF6E56F5) else Color(0xFF2A2A2A)
                    drawRoundRect(
                        color = color, topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                points.forEach { point ->
                    Text(point.label, color = Color(0xFF666666), fontSize = 10.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ── Category row with animated bar ───────────────────────────────────────────
@Composable
private fun AnimatedCategoryRow(icon: String, label: String, amount: Double, percentage: Float, color: Color, modifier: Modifier = Modifier) {
    val animWidth by animateFloatAsState(
        targetValue = percentage.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing), label = "catbar"
    )
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(amount.formatAmount(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("${(percentage * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxWidth(animWidth).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(color))
            }
        }
    }
}

@Composable
private fun StatCard(label: String, amount: Double, color: Color, arrow: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color.copy(0.1f)), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$arrow $label", style = MaterialTheme.typography.bodySmall, color = color.copy(0.8f), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(amount.formatAmount(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
