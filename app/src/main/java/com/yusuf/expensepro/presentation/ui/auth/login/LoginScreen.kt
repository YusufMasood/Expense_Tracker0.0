package com.yusuf.expensepro.presentation.ui.auth.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val CardBg       = Color(0xFF111111)
private val AccentGreen  = Color(0xFF00C853)
//private val AccentPurple = Color(0xFF6E56F5)
//private val TextGray     = Color(0xFF888888)
//private val ErrorRed     = Color(0xFFE53935)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSkipClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto-login if session persists
    LaunchedEffect(Unit) {
        if (viewModel.isAlreadyLoggedIn) onLoginSuccess()
    }
    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onLoginSuccess() }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result -> viewModel.handleGoogleSignInResult(result) }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF0A0A0A))))) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text("💸", fontSize = 64.sp)
            Spacer(Modifier.height(8.dp))
            Text("Welcome Back", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Sign in to continue", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 28.dp))

            // Error banner
            AnimatedVisibility(visible = state.error != null, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
                Surface(shape = RoundedCornerShape(12.dp), color = ErrorRed.copy(0.13f), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(state.error ?: "", color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AuthTextField(state.email, viewModel::onEmailChange, "Email", Icons.Default.Email, state.emailError, keyboardType = KeyboardType.Email)
                    AuthTextField(state.password, viewModel::onPasswordChange, "Password", Icons.Default.Lock, state.passwordError, isPassword = true, passwordVisible = state.isPasswordVisible, onTogglePassword = viewModel::togglePasswordVisibility)
                    Text("Forgot Password?", color = AccentPurple, fontSize = 13.sp, modifier = Modifier.align(Alignment.End).clickable { onForgotPasswordClick() })

                    // Login button
                    Button(onClick = viewModel::login, enabled = !state.isLoading && !state.isGoogleLoading, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)) {
                        if (state.isLoading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text("Sign In", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Divider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFF222222))
                        Text("  or continue with  ", color = TextGray, fontSize = 12.sp)
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFF222222))
                    }

                    // Google Sign-In
                    OutlinedButton(
                        onClick = { googleLauncher.launch(viewModel.googleSignInClient.signInIntent) },
                        enabled = !state.isLoading && !state.isGoogleLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
                    ) {
                        if (state.isGoogleLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text("Sign in with Google", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row {
                Text("Don't have an account? ", color = TextGray, fontSize = 14.sp)
                Text("Register", color = AccentGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onRegisterClick() })
            }
            Spacer(Modifier.height(8.dp))
            Text("Skip for now →", color = Color(0xFF444444), fontSize = 12.sp, modifier = Modifier.clickable { onSkipClick() }.padding(8.dp))
            Spacer(Modifier.height(32.dp))
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
        leadingIcon = { Icon(icon, null, tint = if (error != null) ErrorRed else AccentPurple) },
        trailingIcon = if (isPassword) {{
            IconButton(onClick = { onTogglePassword?.invoke() }) {
                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = TextGray)
            }
        }} else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        isError = error != null,
        supportingText = error?.let {{ Text(it, color = ErrorRed) }},
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentPurple, unfocusedBorderColor = Color(0xFF2A2A2A),
            errorBorderColor = ErrorRed, focusedTextColor = Color.White,
            unfocusedTextColor = Color.White, focusedLabelColor = AccentPurple,
            unfocusedLabelColor = TextGray, cursorColor = AccentPurple
        )
    )
}

private val ErrorRed = Color(0xFFE53935)
private val AccentPurple = Color(0xFF6E56F5)
private val TextGray = Color(0xFF888888)
