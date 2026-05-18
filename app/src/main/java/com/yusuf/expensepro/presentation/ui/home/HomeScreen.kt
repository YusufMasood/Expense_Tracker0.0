package com.yusuf.expensepro.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusuf.expensepro.presentation.theme.ExpenseRed
import com.yusuf.expensepro.presentation.theme.IncomeGreen
import com.yusuf.expensepro.presentation.ui.components.EmptyState
import com.yusuf.expensepro.presentation.ui.components.TransactionItem
import com.yusuf.expensepro.util.formatAmount
import com.yusuf.expensepro.util.formatMonthYear
import java.time.LocalDate

private val SplitYellow = Color(0xFFFFB300)

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val now = LocalDate.now()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Add") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 100.dp)) {

            // ── Balance Header ────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                        .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)))
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Column {
                        // Top row: greeting + profile
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Good ${greeting()} 👋", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(0.75f))
                            IconButton(onClick = onProfileClick, modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(0.12f))) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Total Balance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(0.7f))
                        Text(state.totalBalance.formatAmount(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        if (state.splitNetBalance != 0.0) {
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Cash: ${state.transactionBalance.formatAmount()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(0.6f))
                                Text("Split: ${if (state.splitNetBalance >= 0) "+" else ""}${state.splitNetBalance.formatAmount()}", style = MaterialTheme.typography.bodySmall, color = if (state.splitNetBalance >= 0) IncomeGreen.copy(0.9f) else SplitYellow.copy(0.9f))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(now.formatMonthYear(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(0.55f))
                    }
                }
            }

            // ── Income / Expense ───────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniCard("↑ Income", state.monthlyIncome, IncomeGreen, Modifier.weight(1f))
                    MiniCard("↓ Expenses", state.monthlyExpense, ExpenseRed, Modifier.weight(1f))
                }
            }

            // ── Split Pending ─────────────────────────────────────────
            if (state.splitToReceive > 0.01 || state.splitOwed > 0.01) {
                item {
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (state.splitToReceive > 0.01)
                            SplitPill("↑ Pending to Receive", state.splitToReceive, IncomeGreen, Icons.Default.CallReceived, Modifier.weight(1f))
                        if (state.splitOwed > 0.01)
                            SplitPill("↓ Pending Debt", state.splitOwed, SplitYellow, Icons.Default.CallMade, Modifier.weight(1f))
                    }
                }
            }

            // ── Quick Actions ────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickChip(Icons.Default.BarChart, "Stats", {}, Modifier.weight(1f))
                    QuickChip(Icons.Default.AccountBalance, "Budget", {}, Modifier.weight(1f))
                    QuickChip(Icons.Default.List, "All", onSeeAllTransactions, Modifier.weight(1f))
                    QuickChip(Icons.Default.CallSplit, "Split", {}, Modifier.weight(1f))
                }
            }

            // ── Recent Transactions ──────────────────────────────────
            item {
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onSeeAllTransactions) { Text("See all") }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (state.recentTransactions.isEmpty()) {
                item { EmptyState("No transactions yet.\nTap + to add one!", modifier = Modifier.padding(32.dp)) }
            } else {
                items(state.recentTransactions, key = { it.id }) { tx ->
                    TransactionItem(tx, onClick = { onTransactionClick(tx.id) }, onDelete = { viewModel.deleteTransaction(tx) }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }
        }
    }
}

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when { hour < 12 -> "Morning"; hour < 17 -> "Afternoon"; else -> "Evening" }
}

@Composable private fun MiniCard(label: String, amount: Double, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color.copy(0.1f)), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = color.copy(0.8f), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(amount.formatAmount(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable private fun SplitPill(label: String, amount: Double, color: Color, icon: ImageVector, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = color.copy(0.08f)), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(0.75f))
                Text(amount.formatAmount(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable private fun QuickChip(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    OutlinedCard(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, label, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(3.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
