package com.yusuf.expensepro.presentation.ui.auth.forgotpassword

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusuf.expensepro.presentation.ui.auth.login.AuthTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, viewModel: ForgotPasswordViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF0A0A0A))))) {
        IconButton(onClick = onBack, modifier = Modifier.padding(16.dp).statusBarsPadding()) {
            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
        }
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.isSuccess) {
                Text("📧", fontSize = 68.sp)
                Spacer(Modifier.height(16.dp))
                Text("Email Sent!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Check your inbox for the password reset link.\nCheck spam if you don't see it.", color = Color(0xFF888888), textAlign = TextAlign.Center)
                Spacer(Modifier.height(36.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))) {
                    Text("Back to Login", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("🔑", fontSize = 60.sp)
                Spacer(Modifier.height(8.dp))
                Text("Forgot Password", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Enter your email to receive a reset link", color = Color(0xFF888888), fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp, bottom = 28.dp), textAlign = TextAlign.Center)

                AnimatedVisibility(visible = state.error != null) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE53935).copy(0.13f), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(state.error ?: "", color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                AuthTextField(state.email, viewModel::onEmailChange, "Email", Icons.Default.Email, state.emailError, keyboardType = KeyboardType.Email)
                Spacer(Modifier.height(24.dp))
                Button(onClick = viewModel::sendReset, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))) {
                    if (state.isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    else Text("Send Reset Link", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
