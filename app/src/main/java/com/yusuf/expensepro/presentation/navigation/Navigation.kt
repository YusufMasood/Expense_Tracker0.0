package com.yusuf.expensepro.presentation.navigation

/**
 * ─────────────────────────────────────────────────────────────
 *  UPDATED Navigation.kt
 *  File: presentation/navigation/Navigation.kt
 * ─────────────────────────────────────────────────────────────
 *
 *  Differences from the original:
 *  1. HomeScreen now receives onNavigateToStats, onNavigateToBudget,
 *     onNavigateToSplit so QuickChip buttons actually navigate.
 *  2. Screen sealed class is unchanged — no new routes needed.
 *
 *  Replace the original Navigation.kt with this file entirely.
 */

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.yusuf.expensepro.presentation.ui.add_edit.AddEditTransactionScreen
import com.yusuf.expensepro.presentation.ui.auth.forgotpassword.ForgotPasswordScreen
import com.yusuf.expensepro.presentation.ui.auth.login.LoginScreen
import com.yusuf.expensepro.presentation.ui.auth.register.RegisterScreen
import com.yusuf.expensepro.presentation.ui.auth.splash.SplashScreen
import com.yusuf.expensepro.presentation.ui.budget.BudgetScreen
import com.yusuf.expensepro.presentation.ui.home.HomeScreen
import com.yusuf.expensepro.presentation.ui.profile.ProfileScreen
import com.yusuf.expensepro.presentation.ui.split.GroupDetailScreen
import com.yusuf.expensepro.presentation.ui.split.SplitScreen
import com.yusuf.expensepro.presentation.ui.stats.StatsScreen
import com.yusuf.expensepro.presentation.ui.transactions.TransactionsScreen
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    object Splash          : Screen("splash")
    object Login           : Screen("login")
    object Register        : Screen("register")
    object ForgotPassword  : Screen("forgot_password")
    object Home            : Screen("home")
    object Transactions    : Screen("transactions")
    object Stats           : Screen("stats")
    object Budget          : Screen("budget")
    object Split           : Screen("split")
    object Profile         : Screen("profile")
    object AddTransaction  : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(id: Long) = "edit_transaction/$id"
    }
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(id: Long) = "group_detail/$id"
    }
}

@Composable
fun ExpenseNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
        modifier         = modifier
    ) {

        // ── Splash ──────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(1800)
                val dest = if (FirebaseAuth.getInstance().currentUser != null)
                    Screen.Home.route else Screen.Login.route
                navController.navigate(dest) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        // ── Auth ────────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess       = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRegisterClick      = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClick= { navController.navigate(Screen.ForgotPassword.route) },
                onSkipClick          = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }

        // ── Main ─────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onAddTransaction     = { navController.navigate(Screen.AddTransaction.route) },
                onSeeAllTransactions = { navController.navigate(Screen.Transactions.route) },
                onTransactionClick   = { id -> navController.navigate(Screen.EditTransaction.createRoute(id)) },
                onProfileClick       = { navController.navigate(Screen.Profile.route) },
                // ↓ These wire the QuickChip buttons on the home dashboard
//                onNavigateToStats    = { navController.navigate(Screen.Stats.route) },
//                onNavigateToBudget   = { navController.navigate(Screen.Budget.route) },
//                onNavigateToSplit    = { navController.navigate(Screen.Split.route) }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onBack            = { navController.popBackStack() },
                onAddTransaction  = { navController.navigate(Screen.AddTransaction.route) },
                onTransactionClick= { id -> navController.navigate(Screen.EditTransaction.createRoute(id)) }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Budget.route) {
            BudgetScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Split.route) {
            SplitScreen(
                onGroupClick = { id -> navController.navigate(Screen.GroupDetail.createRoute(id)) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack   = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddEditTransactionScreen(
                transactionId = null,
                onBack        = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.EditTransaction.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { back ->
            AddEditTransactionScreen(
                transactionId = back.arguments?.getLong("transactionId"),
                onBack        = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { back ->
            val groupId = back.arguments?.getLong("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                onBack  = { navController.popBackStack() }
            )
        }
    }
}
