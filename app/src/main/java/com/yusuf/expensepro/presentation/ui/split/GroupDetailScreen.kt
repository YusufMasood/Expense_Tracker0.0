package com.yusuf.expensepro.presentation.ui.split

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusuf.expensepro.domain.model.*
import com.yusuf.expensepro.presentation.theme.ExpenseRed
import com.yusuf.expensepro.util.formatAmount

private val AmoledBg     = Color(0xFF000000)
private val CardDark     = Color(0xFF111111)
private val CardDark2    = Color(0xFF1A1A1A)
private val TextWhite    = Color(0xFFFFFFFF)
private val TextGray     = Color(0xFF888888)
private val AccentGreen  = Color(0xFF00C853)
private val DebtYellow   = Color(0xFFFFB300)
private val PurpleAccent = Color(0xFF6E56F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Long,
    onBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(groupId) { viewModel.loadGroup(groupId) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var paidByExpanded   by remember { mutableStateOf(false) }
    var settleTargetExpanded by remember { mutableStateOf(false) }

    // ── Add Member Dialog ────────────────────────────────────────────────────
    if (state.showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideAddMemberDialog,
            containerColor = CardDark,
            title = { Text("Add Member", color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = state.newMemberName, onValueChange = viewModel::onNewMemberNameChange,
                    label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = darkFieldColors()
                )
            },
            confirmButton = { Button(onClick = viewModel::addMember, enabled = state.newMemberName.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)) { Text("Add", color = Color.Black) } },
            dismissButton = { TextButton(onClick = viewModel::hideAddMemberDialog) { Text("Cancel", color = TextGray) } }
        )
    }

    // ── Settle Sheet ─────────────────────────────────────────────────────────
    if (state.showSettleSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::hideSettleSheet, containerColor = CardDark) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Settle Up", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)
                val target = state.settleTargetMember
                if (target != null) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1A1A1A)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Settling with", color = TextGray, fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(target.name, color = AccentGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                OutlinedTextField(value = state.settleAmount, onValueChange = viewModel::onSettleAmountChange, label = { Text("Amount (₹)") }, isError = state.settleAmountError != null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true, colors = darkFieldColors())
                state.settleAmountError?.let { Text(it, color = ExpenseRed, fontSize = 12.sp) }
                OutlinedTextField(value = state.settleNote, onValueChange = viewModel::onSettleNoteChange, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth(), colors = darkFieldColors())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.isPartialSettle, onCheckedChange = { viewModel.onPartialSettleToggle() }, colors = CheckboxDefaults.colors(checkedColor = AccentGreen))
                    Text("Partial payment", color = TextGray, fontSize = 13.sp)
                }
                Button(onClick = viewModel::confirmSettle, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)) {
                    Text("Confirm Settlement", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ── Add Expense Sheet ────────────────────────────────────────────────────
    if (state.showAddExpenseSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::hideAddExpenseSheet, containerColor = CardDark) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Add Split Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)

                OutlinedTextField(value = state.expenseTitle, onValueChange = viewModel::onExpenseTitleChange, label = { Text("What was it for?") }, isError = state.expenseTitleError != null, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = darkFieldColors())
                OutlinedTextField(value = state.expenseAmount, onValueChange = viewModel::onExpenseAmountChange, label = { Text("Total Amount (₹)") }, isError = state.expenseAmountError != null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true, colors = darkFieldColors())

                // Paid By
                ExposedDropdownMenuBox(expanded = paidByExpanded, onExpandedChange = { paidByExpanded = it }) {
                    OutlinedTextField(
                        value = state.members.find { it.id == state.expensePaidBy }?.let { if (it.isCurrentUser) "${it.name} (You)" else it.name } ?: "Select",
                        onValueChange = {}, readOnly = true, label = { Text("Paid by") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(paidByExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), colors = darkFieldColors()
                    )
                    ExposedDropdownMenu(expanded = paidByExpanded, onDismissRequest = { paidByExpanded = false }) {
                        state.members.forEach { member ->
                            Box(Modifier.background(CardDark2)) {
                                DropdownMenuItem(text = { Text(if (member.isCurrentUser) "${member.name} (You)" else member.name, color = TextWhite) }, onClick = { viewModel.onExpensePaidByChange(member.id); paidByExpanded = false }, colors = MenuDefaults.itemColors(textColor = TextWhite))
                            }
                        }
                    }
                }

                // Category
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(value = "${state.expenseCategory.icon} ${state.expenseCategory.label}", onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = darkFieldColors())
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        Category.entries.forEach { cat ->
                            Box(Modifier.background(CardDark2)) {
                                DropdownMenuItem(text = { Text("${cat.icon} ${cat.label}", color = TextWhite) }, onClick = { viewModel.onExpenseCategoryChange(cat); categoryExpanded = false }, colors = MenuDefaults.itemColors(textColor = TextWhite))
                            }
                        }
                    }
                }

                // Split Type selector
                Text("Split Type", color = TextGray, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SplitType.entries.forEach { type ->
                        FilterChip(
                            selected = state.expenseSplitType == type,
                            onClick = { viewModel.onSplitTypeChange(type) },
                            label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGreen, selectedLabelColor = Color.Black)
                        )
                    }
                }

                // Per-member input for custom/percentage/shares
                if (state.expenseSplitType != SplitType.EQUAL) {
                    val hint = when (state.expenseSplitType) {
                        SplitType.CUSTOM -> "Amount"
                        SplitType.PERCENTAGE -> "Percent %"
                        SplitType.SHARES -> "Shares"
                        else -> ""
                    }
                    state.memberSplitInputs.forEach { input ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(if (input.member.isCurrentUser) "${input.member.name} (You)" else input.member.name, color = TextWhite, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            OutlinedTextField(
                                value = input.value,
                                onValueChange = { viewModel.onMemberSplitInputChange(input.member.id, it) },
                                label = { Text(hint) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = darkFieldColors()
                            )
                        }
                    }
                    state.splitInputError?.let { Text(it, color = ExpenseRed, fontSize = 12.sp) }
                }

                // Equal split preview
                if (state.expenseSplitType == SplitType.EQUAL) {
                    val amount = state.expenseAmount.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && state.members.isNotEmpty()) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1E1E1E)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${state.members.size} people equally", color = TextGray, style = MaterialTheme.typography.bodySmall)
                                Text("${(amount / state.members.size).formatAmount()} each", color = AccentGreen, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Button(onClick = viewModel::saveExpense, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)) {
                    Text("Add Expense", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ── Main Scaffold ────────────────────────────────────────────────────────
    Scaffold(
        containerColor = AmoledBg,
        topBar = {
            TopAppBar(
                title = { Text(state.group?.name ?: "Group", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) } },
                actions = { IconButton(onClick = viewModel::showAddMemberDialog) { Icon(Icons.Default.PersonAdd, null, tint = TextWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddExpenseSheet, containerColor = AccentGreen, contentColor = Color.Black) {
                Icon(Icons.Default.Add, "Add expense")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {

            // ── Balance banner ───────────────────────────────────────────────
            item {
                val me = state.memberBalances.find { it.member.isCurrentUser }
                if (me != null) {
                    Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), color = CardDark) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            val net = me.net
                            Text(
                                text = when {
                                    net > 0.01  -> "You are owed ${net.formatAmount()} overall"
                                    net < -0.01 -> "You owe ${(-net).formatAmount()} overall"
                                    else        -> "✓ All settled up"
                                },
                                color = when { net > 0.01 -> AccentGreen; net < -0.01 -> DebtYellow; else -> TextGray },
                                fontWeight = FontWeight.Bold, fontSize = 16.sp
                            )
                            if (kotlin.math.abs(net) > 0.01) {
                                Spacer(Modifier.height(10.dp))
                                state.memberBalances.filter { !it.member.isCurrentUser }.forEach { balance ->
                                    val netWithMe = -balance.net
                                    if (kotlin.math.abs(netWithMe) > 0.01) {
                                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (netWithMe > 0) "You owe ${balance.member.name}  " else "${balance.member.name} owes you  ", color = TextGray, fontSize = 13.sp)
                                            Text(kotlin.math.abs(netWithMe).formatAmount(), color = if (netWithMe > 0) DebtYellow else AccentGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            if (netWithMe < -0.01) { // they owe you → settle button
                                                Spacer(Modifier.width(8.dp))
                                                TextButton(onClick = { viewModel.showSettleSheet(balance.member) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                                                    Text("Settle", color = AccentGreen, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Settle up button for when you owe ───────────────────────────
            item {
                val me = state.memberBalances.find { it.member.isCurrentUser }
                if (me != null && me.net < -0.01) {
                    val creditor = state.memberBalances.filter { !it.member.isCurrentUser && it.net > 0.01 }.maxByOrNull { it.net }
                    if (creditor != null) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Button(
                                onClick = { viewModel.showSettleSheet(creditor.member) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) { Text("Settle up with ${creditor.member.name}", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }

            // ── Settlement history ───────────────────────────────────────────
            if (state.settlements.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Settlements", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
                items(state.settlements) { settlement ->
                    val fromName = state.members.find { it.id == settlement.fromMemberId }?.let { if (it.isCurrentUser) "You" else it.name } ?: "?"
                    val toName   = state.members.find { it.id == settlement.toMemberId }?.name ?: "?"
                    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFF0D1A0D)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("$fromName paid $toName ${settlement.amount.formatAmount()}${if (settlement.isPartial) " (partial)" else ""}", color = AccentGreen, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(settlement.date.toString(), color = TextGray, fontSize = 10.sp)
                        }
                    }
                }
            }

            // ── Expenses header ──────────────────────────────────────────────
            item { Spacer(Modifier.height(12.dp)) }

            val grouped = state.expensesWithShares.groupBy {
                val d = it.expense.date
                "${d.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${d.year}"
            }

            if (state.expensesWithShares.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🧾", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No expenses yet", color = TextGray)
                            Text("Tap + to add the first one", color = Color(0xFF555555), fontSize = 12.sp)
                        }
                    }
                }
            } else {
                grouped.forEach { (monthLabel, expenses) ->
                    item { Text(monthLabel, color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
                    items(expenses, key = { it.expense.id }) { item ->
                        ExpenseRow(item = item, currentUserId = state.members.find { it.isCurrentUser }?.id ?: -1L, onDelete = { viewModel.deleteExpense(item.expense) }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(item: ExpenseWithShares, currentUserId: Long, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    var showDelete by remember { mutableStateOf(false) }
    if (showDelete) {
        AlertDialog(onDismissRequest = { showDelete = false }, containerColor = Color(0xFF161616), title = { Text("Delete?", color = Color.White) }, text = { Text("Delete \"${item.expense.title}\"?", color = Color(0xFF888888)) },
            confirmButton = { TextButton(onClick = { showDelete = false; onDelete() }) { Text("Delete", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel", color = Color(0xFF888888)) } })
    }
    val myShare      = item.shares.find { it.memberId == currentUserId }?.shareAmount ?: 0.0
    val iMadePayment = item.expense.paidByMemberId == currentUserId
    val notInvolved  = myShare == 0.0 && !iMadePayment

    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = CardDark) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.width(36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(item.expense.date.dayOfMonth.toString(), color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(item.expense.date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }, color = Color(0xFF555555), fontSize = 10.sp)
            }
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1E1E1E)), contentAlignment = Alignment.Center) {
                Text(item.expense.category.icon, fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.expense.title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    // Split type badge
                    if (item.expense.splitType != SplitType.EQUAL) {
                        Surface(shape = RoundedCornerShape(4.dp), color = PurpleAccent.copy(0.15f), modifier = Modifier.padding(start = 4.dp)) {
                            Text(item.expense.splitType.name.take(3), color = PurpleAccent, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                Text(if (iMadePayment) "You paid ${item.expense.totalAmount.formatAmount()}" else "${item.paidByName} paid ${item.expense.totalAmount.formatAmount()}", color = Color(0xFF666666), fontSize = 11.sp)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                when {
                    notInvolved  -> Text("not involved", color = Color(0xFF555555), fontSize = 11.sp)
                    iMadePayment && myShare < item.expense.totalAmount -> {
                        Text("you lent", color = TextGray, fontSize = 10.sp)
                        Text((item.expense.totalAmount - myShare).formatAmount(), color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    !iMadePayment -> {
                        Text("you borrowed", color = TextGray, fontSize = 10.sp)
                        Text(myShare.formatAmount(), color = DebtYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    else -> {
                        Text("you paid", color = TextGray, fontSize = 10.sp)
                        Text(item.expense.totalAmount.formatAmount(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            IconButton(onClick = { showDelete = true }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFF333333), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PurpleAccent, unfocusedBorderColor = Color(0xFF2A2A2A),
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedLabelColor = PurpleAccent, unfocusedLabelColor = Color(0xFF666666), cursorColor = PurpleAccent
)
