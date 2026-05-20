package com.yusuf.expensepro.presentation.ui.split

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusuf.expensepro.domain.model.SplitGroup
import com.yusuf.expensepro.presentation.theme.ExpenseRed
import com.yusuf.expensepro.presentation.theme.IncomeGreen
import com.yusuf.expensepro.util.formatAmount

val SplitYellow = Color(0xFFFFB300)

private val groupIconOptions = listOf("👥","🏠","✈️","🎉","🍽️","🏕️","💼","🎮","🏋️","🎬","🛒","🎸")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(
    onGroupClick: (Long) -> Unit,
    viewModel: SplitViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.showCreateDialog) {
        CreateGroupDialog(
            name = state.newGroupName,
            icon = state.newGroupIcon,
            onNameChange = viewModel::onGroupNameChange,
            onIconChange = viewModel::onGroupIconChange,
            onConfirm = viewModel::createGroup,
            onDismiss = viewModel::hideCreateDialog
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::showCreateDialog,
                icon = { Icon(Icons.Default.GroupAdd, "New group") },
                text = { Text("New Group") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header summary banner
            item {
                SplitSummaryBanner(
                    toReceive = state.totalToReceive,
                    owes = state.totalOwed
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Your Groups",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.groupsWithBalance.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🤝", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No split groups yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            "Create a group to split expenses with friends",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                items(state.groupsWithBalance, key = { it.group.id }) { item ->
                    GroupCard(
                        group = item.group,
                        myNet = item.myNet,
                        onClick = { onGroupClick(item.group.id) },
                        onDelete = { viewModel.deleteGroup(item.group) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitSummaryBanner(toReceive: Double, owes: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // To Receive
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("↑ To Receive", style = MaterialTheme.typography.bodySmall, color = IncomeGreen.copy(0.8f), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(toReceive.formatAmount(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = IncomeGreen)
            }
        }
        // You Owe
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SplitYellow.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("↓ You Owe", style = MaterialTheme.typography.bodySmall, color = SplitYellow.copy(0.8f), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(owes.formatAmount(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SplitYellow)
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: SplitGroup,
    myNet: Double,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Group") },
            text = { Text("Delete \"${group.name}\" and all its expenses?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(group.icon, fontSize = 26.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        myNet > 0.01 -> "You're owed ${myNet.formatAmount()}"
                        myNet < -0.01 -> "You owe ${(-myNet).formatAmount()}"
                        else -> "All settled up ✓"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        myNet > 0.01 -> IncomeGreen
                        myNet < -0.01 -> SplitYellow
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
            }
            Icon(
                Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(0.3f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateGroupDialog(
    name: String,
    icon: String,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Split Group") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Group name") },
                    placeholder = { Text("e.g. Goa Trip, Flat mates") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Choose Icon", style = MaterialTheme.typography.labelMedium)
                // Simple icon grid
                val rows = groupIconOptions.chunked(6)
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { emoji ->
                            Surface(
                                onClick = { onIconChange(emoji) },
                                shape = CircleShape,
                                color = if (icon == emoji) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = name.isNotBlank()) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
