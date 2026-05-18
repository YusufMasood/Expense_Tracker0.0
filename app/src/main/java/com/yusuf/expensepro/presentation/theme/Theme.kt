package com.yusuf.expensepro.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val ExpenseRed   = Color(0xFFE53935)
val IncomeGreen  = Color(0xFF43A047)
val BudgetAmber  = Color(0xFFFFA000)

// AMOLED dark — true black backgrounds for battery saving on OLED
private val AmoledDarkColorScheme = darkColorScheme(
    primary          = Color(0xFF00C853),
    onPrimary        = Color(0xFF000000),
    primaryContainer = Color(0xFF003918),
    onPrimaryContainer = Color(0xFF9BFFBA),
    secondary        = Color(0xFF6E56F5),
    onSecondary      = Color.White,
    background       = Color(0xFF000000),   // true AMOLED black
    onBackground     = Color(0xFFE0E0E0),
    surface          = Color(0xFF111111),   // near-black cards
    onSurface        = Color(0xFFE0E0E0),
    surfaceVariant   = Color(0xFF1C1C1C),
    onSurfaceVariant = Color(0xFFAAAAAA),
    error            = ExpenseRed,
    onError          = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary          = Color(0xFF1A73E8),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    secondary        = Color(0xFF5F6368),
    background       = Color(0xFFF8F9FA),
    surface          = Color.White,
    surfaceVariant   = Color(0xFFF1F3F4),
    error            = ExpenseRed,
)

val ExpenseTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 36.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 28.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 16.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp, letterSpacing = 0.5.sp),
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = true,           // Default AMOLED dark
    dynamicColor: Boolean = false,       // Disable dynamic — keep brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> AmoledDarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = ExpenseTypography,
        content     = content
    )
}
