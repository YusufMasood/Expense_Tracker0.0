package com.yusuf.expensepro.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val AmoledBg = Color(0xFF000000)
private val CardDark = Color(0xFF111111)
private val TextGray = Color(0xFF888888)
private val AccentGreen = Color(0xFF00C853)
private val ErrorRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = CardDark,
            title = { Text("Logout", color = Color.White) },
            text = { Text("Are you sure you want to logout?", color = TextGray) },
            confirmButton = {
                Button(onClick = { viewModel.logout(); onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel", color = TextGray) } }
        )
    }

    Scaffold(
        containerColor = AmoledBg,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar + Name
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CardDark), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF1A1A2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = state.profile?.fullName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                        Text(initial, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(state.profile?.fullName ?: "Guest User", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(state.profile?.email ?: "", color = TextGray, fontSize = 14.sp)
                    if (state.profile?.phoneNumber?.isNotBlank() == true) {
                        Text(state.profile?.phoneNumber ?: "", color = TextGray, fontSize = 13.sp)
                    }
                }
            }

            // Edit profile
            if (state.isEditing) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardDark), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Edit Profile", color = Color.White, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = state.editName, onValueChange = viewModel::onNameChange,
                            label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
                            colors = darkFieldColors()
                        )
                        OutlinedTextField(
                            value = state.editPhone, onValueChange = viewModel::onPhoneChange,
                            label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                            colors = darkFieldColors()
                        )
                        if (state.updateError != null) {
                            Text(state.updateError!!, color = ErrorRed, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = viewModel::cancelEdit, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                Text("Cancel", color = TextGray)
                            }
                            Button(onClick = viewModel::saveProfile, enabled = !state.isLoading, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)) {
                                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                else Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Actions
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardDark), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ProfileAction(Icons.Default.Edit, "Edit Profile", Color(0xFF6E56F5)) { viewModel.startEdit() }
                    Divider(color = Color(0xFF1A1A1A))
                    ProfileAction(Icons.Default.Security, "Security Settings", Color(0xFF00BCD4)) {}
                    Divider(color = Color(0xFF1A1A1A))
                    ProfileAction(Icons.Default.CloudSync, "Sync Status", AccentGreen) {}
                    Divider(color = Color(0xFF1A1A1A))
                    ProfileAction(Icons.Default.Logout, "Logout", ErrorRed) { showLogoutDialog = true }
                }
            }

            // App info
            Text("Expense Pro v1.0 • Offline-first", color = Color(0xFF333333), fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun ProfileAction(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(label, color = Color.White, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF444444), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF6E56F5), unfocusedBorderColor = Color(0xFF2A2A2A),
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedLabelColor = Color(0xFF6E56F5), unfocusedLabelColor = Color(0xFF666666)
)
