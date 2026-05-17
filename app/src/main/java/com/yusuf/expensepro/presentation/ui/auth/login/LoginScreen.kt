package com.yusuf.expensepro.presentation.ui.auth.login

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val CardBg = Color(0xFF161616)
private val AccentGreen = Color(0xFF26D100)
private val TextGray = Color(0xFF888888)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSkipClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onLoginSuccess() }

    Box(modifier = Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(Color(0xFF0D0D0D),
            Color(0xFF0A0A0A))))) {
        Column(modifier = Modifier.fillMaxSize()
            .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💸", fontSize = 56.sp)
            Spacer(Modifier.height(8.dp))
            Text("Expense Pro", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("Track Smart. Split Easy.", color = TextGray,
                fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 32.dp))

            AnimatedVisibility(visible = state.error != null) {
                Surface(shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE53935).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline,
                            null, tint = Color(0xFFE53935),
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(state.error ?: "", color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    AuthTextField(value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email", icon = Icons.Default.Email,
                        error = state.emailError, keyboardType = KeyboardType.Email)
                    Spacer(Modifier.height(16.dp))
                    AuthTextField(value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Password", icon = Icons.Default.Lock,
                        error = state.passwordError, isPassword = true,
                        passwordVisible = state.isPasswordVisible,
                        onTogglePassword = viewModel::togglePasswordVisibility)
                    Text("Forgot Password?", color = TextGray,
                        fontSize = 13.sp, modifier = Modifier.align(Alignment.End)
                            .padding(top = 10.dp)
                            .clickable { onForgotPasswordClick() })
                    Spacer(Modifier.height(28.dp))
                    Button(onClick = viewModel::login, enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)) {
                        if (state.isLoading) CircularProgressIndicator(color = Color.Black,
                            modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text("Login", color = Color.Black,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row {
                Text("Don't have an account? ", color = TextGray, fontSize = 14.sp)
                Text("Register", color = AccentGreen,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onRegisterClick() })
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = onSkipClick) {
                Text("Skip")
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String, onValueChange: (String) -> Unit, label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    error: String? = null, isPassword: Boolean = false,
    passwordVisible: Boolean = false, onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon,
            null,
            tint = if (error != null) Color(0xFFE53935) else Color(0xFF6E56F5)) },
        trailingIcon = if (isPassword) {{ IconButton(onClick = {
            onTogglePassword?.invoke() }) {
            Icon(if (passwordVisible)
                Icons.Default.Visibility else Icons.Default.VisibilityOff,
                null, tint = Color(0xFF888888)) } }} else null,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        isError = error != null,
        supportingText = error?.let { { Text(it, color = Color(0xFFE53935)) } },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults
            .colors(focusedBorderColor = Color(0xFF6E56F5),
                unfocusedBorderColor = Color(0xFF333333),
                errorBorderColor = Color(0xFFE53935),
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF6E56F5),
                unfocusedLabelColor = Color(0xFF888888),
                cursorColor = Color(0xFF6E56F5))
    )
}
