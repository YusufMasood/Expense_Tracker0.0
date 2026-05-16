# 💸 Expense Tracker

A production-ready Android expense tracking app built with modern Android architecture.

## Features

- ✅ **Add / Edit / Delete** transactions (income & expenses)
- 🏷️ **12 Categories** — Food, Transport, Shopping, Health, Salary, and more
- 📊 **Stats Screen** — Monthly breakdown by category with progress bars and net savings
- 🎯 **Budget Limits** — Set monthly spending caps per category with over-budget alerts
- 💰 **Income vs Expense** tracking with balance overview
- 📅 **Date picker** for any past or present date
- 🌈 **Material 3** with dynamic color (Android 12+)

## Architecture

```
MVVM + Clean Architecture
├── data/
│   ├── local/          ← Room database, DAOs, entities
│   └── repository/     ← Repository implementation
├── di/                 ← Hilt dependency injection modules
├── domain/
│   └── model/          ← Domain models (Transaction, Budget, Category)
├── presentation/
│   ├── navigation/     ← NavGraph + Screen routes
│   ├── theme/          ← Material 3 theme + colors
│   └── ui/
│       ├── home/       ← Dashboard screen + ViewModel
│       ├── transactions/  ← Transaction list + filters
│       ├── add_edit/   ← Add/Edit transaction form
│       ├── stats/      ← Monthly statistics + charts
│       └── budget/     ← Monthly budget management
└── util/               ← Extension functions
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + StateFlow |
| DI | Hilt |
| Database | Room |
| Async | Kotlin Coroutines + Flow |
| Navigation | Navigation Compose |

## Setup

1. Clone the repo
2. Open in **Android Studio Hedgehog** or newer
3. Sync Gradle
4. Run on a device or emulator (min SDK 26 / Android 8.0)

## Screens

| Screen | Description |
|--------|-------------|
| **Home** | Balance card, monthly income/expense summary, recent 5 transactions |
| **Transactions** | Full list with All / Income / Expense filter chips |
| **Add/Edit** | Form with type toggle, category picker, date picker |
| **Stats** | Month navigator, category breakdown with progress bars, net savings |
| **Budget** | Monthly budget cards with live spend tracking and over-budget warnings |
