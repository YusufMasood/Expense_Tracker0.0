package com.yusuf.expensepro.presentation.ui.auth.register

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.verticalScroll
import com.yusuf.expensepro.presentation.ui.auth.login.AuthTextField

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onLoginClick: () -> Unit, viewModel: RegisterViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onRegisterSuccess() }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF0A0A0A))))) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))
            Text("✨", fontSize = 52.sp)
            Spacer(Modifier.height(8.dp))
            Text("Create Account", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Join Expense Pro today", color = Color(0xFF888888), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 28.dp))

            AnimatedVisibility(visible = state.error != null, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE53935).copy(0.13f), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(state.error ?: "", color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AuthTextField(state.name, viewModel::onNameChange, "Full Name", Icons.Default.Person, state.nameError)
                    AuthTextField(state.email, viewModel::onEmailChange, "Email", Icons.Default.Email, state.emailError, keyboardType = KeyboardType.Email)
                    AuthTextField(state.password, viewModel::onPasswordChange, "Password", Icons.Default.Lock, state.passwordError, isPassword = true, passwordVisible = state.isPasswordVisible, onTogglePassword = viewModel::togglePassword)
                    AuthTextField(state.confirmPassword, viewModel::onConfirmPasswordChange, "Confirm Password", Icons.Default.Lock, state.confirmError, isPassword = true, passwordVisible = state.isConfirmVisible, onTogglePassword = viewModel::toggleConfirm)
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = viewModel::register, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))) {
                        if (state.isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text("Create Account", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row {
                Text("Already have an account? ", color = Color(0xFF888888), fontSize = 14.sp)
                Text("Sign In", color = Color(0xFF00C853), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onLoginClick() })
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
