package com.yusuf.expensepro

/**
 * ─────────────────────────────────────────────────────────────
 *  UPDATED MainActivity.kt
 *  File: app/src/main/java/com/yusuf/expensepro/MainActivity.kt
 * ─────────────────────────────────────────────────────────────
 *
 *  Changes over the original:
 *  - NavigationBar uses NavigationBarItem with proper selected state
 *  - navController passed into HomeScreen so QuickChip actions
 *    navigate correctly (Stats, Budget, Split, All Transactions)
 *  - noNavRoutes uses startsWith for parameterised routes
 */

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.yusuf.expensepro.presentation.navigation.Screen
import com.yusuf.expensepro.presentation.navigation.ExpenseNavGraph
import com.yusuf.expensepro.presentation.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bottomNavItems = listOf(
        BottomNavItem(Screen.Home,    "Home",    Icons.Default.Home),
        BottomNavItem(Screen.Stats,   "Stats",   Icons.Default.BarChart),
        BottomNavItem(Screen.Budget,  "Budget",  Icons.Default.AccountBalance),
        BottomNavItem(Screen.Split,   "Split",   Icons.Default.CallSplit),
        BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person),
    )

    // Auth and full-screen routes where bottom nav should NOT appear
    private val noNavPrefixes = setOf(
        Screen.Splash.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route,
        Screen.AddTransaction.route,
        "edit_transaction",   // prefix match for edit_transaction/{id}
        "group_detail"        // prefix match for group_detail/{id}
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentDestination = currentBackStack?.destination
                val currentRoute = currentDestination?.route ?: ""

                val showBottomBar = noNavPrefixes.none { prefix ->
                    currentRoute == prefix || currentRoute.startsWith("$prefix/")
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                bottomNavItems.forEach { item ->
                                    val selected = currentDestination
                                        ?.hierarchy
                                        ?.any { it.route == item.screen.route } == true

                                    NavigationBarItem(
                                        icon  = { Icon(item.icon, item.label) },
                                        label = { Text(item.label) },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState    = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    ExpenseNavGraph(
                        navController = navController,
                        modifier      = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
