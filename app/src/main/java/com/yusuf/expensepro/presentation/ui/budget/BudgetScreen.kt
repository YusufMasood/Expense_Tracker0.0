package com.yusuf.expensepro.presentation.ui.budget

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusuf.expensepro.domain.model.Category
import com.yusuf.expensepro.presentation.theme.BudgetAmber
import com.yusuf.expensepro.presentation.theme.ExpenseRed
import com.yusuf.expensepro.presentation.theme.IncomeGreen
import com.yusuf.expensepro.presentation.ui.components.EmptyState
import com.yusuf.expensepro.util.formatAmount
import com.yusuf.expensepro.util.formatMonthYear
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(onBack: () -> Unit, viewModel: BudgetViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var categoryExpanded by remember { mutableStateOf(false) }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideDialog,
            title = { Text("Set Monthly Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                        OutlinedTextField(
                            value = "${state.dialogCategory.icon} ${state.dialogCategory.label}",
                            onValueChange = {}, readOnly = true, label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            Category.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${cat.icon} ${cat.label}") },
                                    onClick = { viewModel.onDialogCategoryChange(cat); categoryExpanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.dialogAmount, onValueChange = viewModel::onDialogAmountChange,
                        label = { Text("Budget Limit (₹)") }, isError = state.dialogAmountError != null,
                        supportingText = state.dialogAmountError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
            },
            confirmButton = { Button(onClick = viewModel::saveBudget) { Text("Save") } },
            dismissButton = { TextButton(onClick = viewModel::hideDialog) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Budgets", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::showAddDialog,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Budget") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Text(LocalDate.now().formatMonthYear(), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.padding(vertical = 8.dp))

            if (state.budgetsWithSpend.isEmpty()) {
                EmptyState("No budgets set.\nTap + to set a monthly limit.", modifier = Modifier.fillMaxSize().padding(32.dp))
            } else {
                // Summary header
                val totalBudgeted = state.budgetsWithSpend.sumOf { it.budget.limitAmount }
                val totalSpent    = state.budgetsWithSpend.sumOf { it.spent }
                val overCount     = state.budgetsWithSpend.count { it.isOverBudget }
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                        BudgetStat("Budgeted", totalBudgeted.formatAmount(), MaterialTheme.colorScheme.primary)
                        BudgetStat("Spent", totalSpent.formatAmount(), if (totalSpent > totalBudgeted) ExpenseRed else IncomeGreen)
                        BudgetStat("Over Budget", "$overCount categories", if (overCount > 0) ExpenseRed else IncomeGreen)
                    }
                }

                LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.budgetsWithSpend, key = { it.budget.id }) { item ->
                        AnimatedBudgetCard(item = item, onDelete = { viewModel.deleteBudget(item.budget.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    }
}

@Composable
private fun AnimatedBudgetCard(item: BudgetWithSpend, onDelete: () -> Unit) {
    // Dynamic color based on usage
    val progressColor = when {
        item.isOverBudget      -> ExpenseRed
        item.progress > 0.85f  -> ExpenseRed.copy(alpha = 0.8f)
        item.progress > 0.65f  -> BudgetAmber
        else                   -> IncomeGreen
    }

    // Animate progress bar width
    val animatedProgress by animateFloatAsState(
        targetValue = item.progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing), label = "budget"
    )

    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.budget.category.icon, fontSize = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(item.budget.category.label, fontWeight = FontWeight.SemiBold)
                        Text("${item.spent.formatAmount()} of ${item.budget.limitAmount.formatAmount()}",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Remaining or over badge
                    val remaining = item.budget.limitAmount - item.spent
                    if (item.isOverBudget) {
                        Surface(shape = RoundedCornerShape(8.dp), color = ExpenseRed.copy(0.12f)) {
                            Text("Over ${(-remaining).formatAmount()}", color = ExpenseRed, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    } else {
                        Text("${remaining.formatAmount()} left", color = progressColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.35f))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Animated progress bar
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(progressColor))
            }

            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(item.progress * 100).toInt().coerceAtMost(100)}% used",
                    style = MaterialTheme.typography.labelSmall, color = progressColor, fontWeight = FontWeight.SemiBold)
                Text(if (item.isOverBudget) "🚨 Limit exceeded" else if (item.progress > 0.85f) "⚠️ Almost there" else "✓ On track",
                    style = MaterialTheme.typography.labelSmall, color = progressColor.copy(0.7f))
            }
        }
    }
}
