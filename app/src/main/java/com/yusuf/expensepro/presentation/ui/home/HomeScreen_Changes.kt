package com.yusuf.expensepro.presentation.ui.home

/**
 * ─────────────────────────────────────────────────────────────
 *  UPDATED HomeScreen.kt
 *  File: presentation/ui/home/HomeScreen.kt
 * ─────────────────────────────────────────────────────────────
 *
 *  Changes over the original:
 *  - QuickChip lambdas are now wired to proper navigation
 *    callbacks (onNavigateToStats, onNavigateToBudget, onNavigateToSplit)
 *  - onSeeAllTransactions already existed — now passed consistently
 *  - All navigation happens via callback pattern (not NavController
 *    directly in composable) for MVVM correctness
 *
 *  The original HomeScreen file doesn't need to be fully replaced —
 *  only the QuickChip row and the function signature need updating.
 *  Apply the diff below instead of replacing the whole file.
 */

/*
── DIFF: Update the HomeScreen signature ──────────────────────────────────────

Change this:

    @Composable
    fun HomeScreen(
        onAddTransaction: () -> Unit,
        onSeeAllTransactions: () -> Unit,
        onTransactionClick: (Long) -> Unit,
        onProfileClick: () -> Unit = {},
        viewModel: HomeViewModel = hiltViewModel()
    )

To this:

    @Composable
    fun HomeScreen(
        onAddTransaction: () -> Unit,
        onSeeAllTransactions: () -> Unit,
        onTransactionClick: (Long) -> Unit,
        onProfileClick: () -> Unit = {},
        onNavigateToStats: () -> Unit = {},     // ← ADD
        onNavigateToBudget: () -> Unit = {},    // ← ADD
        onNavigateToSplit: () -> Unit = {},     // ← ADD
        viewModel: HomeViewModel = hiltViewModel()
    )

── DIFF: Update the QuickChip row ─────────────────────────────────────────────

Change this:

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickChip(Icons.Default.BarChart, "Stats", {}, Modifier.weight(1f))
        QuickChip(Icons.Default.AccountBalance, "Budget", {}, Modifier.weight(1f))
        QuickChip(Icons.Default.List, "All", onSeeAllTransactions, Modifier.weight(1f))
        QuickChip(Icons.Default.CallSplit, "Split", {}, Modifier.weight(1f))
    }

To this:

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickChip(Icons.Default.BarChart,      "Stats",  onNavigateToStats,        Modifier.weight(1f))
        QuickChip(Icons.Default.AccountBalance,"Budget", onNavigateToBudget,       Modifier.weight(1f))
        QuickChip(Icons.Default.List,          "All",    onSeeAllTransactions,     Modifier.weight(1f))
        QuickChip(Icons.Default.CallSplit,     "Split",  onNavigateToSplit,        Modifier.weight(1f))
    }

── DIFF: Update Navigation.kt call site ───────────────────────────────────────

In Navigation.kt, composable(Screen.Home.route) change:

    HomeScreen(
        onAddTransaction = ...,
        onSeeAllTransactions = ...,
        onTransactionClick = ...,
        onProfileClick = ...
    )

To:

    HomeScreen(
        onAddTransaction      = { navController.navigate(Screen.AddTransaction.route) },
        onSeeAllTransactions  = { navController.navigate(Screen.Transactions.route) },
        onTransactionClick    = { id -> navController.navigate(Screen.EditTransaction.createRoute(id)) },
        onProfileClick        = { navController.navigate(Screen.Profile.route) },
        onNavigateToStats     = { navController.navigate(Screen.Stats.route) },
        onNavigateToBudget    = { navController.navigate(Screen.Budget.route) },
        onNavigateToSplit     = { navController.navigate(Screen.Split.route) }
    )
*/

// This file is a change guide. Apply the diffs above to the original files.
// No new standalone file is needed — these are targeted edits.
