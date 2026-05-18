package com.yusuf.expensepro.utils

import java.text.NumberFormat
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * App-wide formatting utilities.
 *
 * These extend what's already in util/Extensions.kt.
 * Keeping formatting in one place makes it trivial to
 * switch currency symbol (₹ → $ → €) for internationalisation.
 */

// ── Currency ──────────────────────────────────────────────────────────────────

object CurrencyFormatter {

    // Change this constant to switch the currency symbol across the entire app
    const val SYMBOL = "₹"

    private val indianLocale = Locale("en", "IN")

    /**
     * Formats a double as a currency string.
     * e.g. 12345.6 → "₹12,345.60"
     *      12345.0 → "₹12,345"
     */
    fun format(amount: Double): String {
        return if (amount == kotlin.math.floor(amount)) {
            "$SYMBOL%,.0f".format(amount)
        } else {
            "$SYMBOL%,.2f".format(amount)
        }
    }

    /**
     * Formats with sign prefix.
     * e.g. +₹1,000.00 for income, -₹500.00 for expense
     */
    fun formatSigned(amount: Double, isIncome: Boolean): String {
        val sign = if (isIncome) "+" else "-"
        return "$sign${format(kotlin.math.abs(amount))}"
    }

    /**
     * Parses a raw string (possibly with currency symbol) to Double.
     * Returns null if the string is not a valid number.
     */
    fun parse(raw: String): Double? =
        raw.replace(SYMBOL, "").replace(",", "").trim().toDoubleOrNull()
}

// ── Date ──────────────────────────────────────────────────────────────────────

object DateFormatter {

    private val SHORT  = DateTimeFormatter.ofPattern("d MMM")
    private val MEDIUM = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val LONG   = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
    private val MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy")

    fun LocalDate.toShortDisplay(): String = this.format(SHORT)      // "5 Jan"
    fun LocalDate.toDisplay(): String = this.format(MEDIUM)           // "5 Jan 2025"
    fun LocalDate.toLongDisplay(): String = this.format(LONG)         // "Sunday, 5 January 2025"
    fun LocalDate.toMonthYear(): String = this.format(MONTH_YEAR)     // "January 2025"

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()
    fun isYesterday(date: LocalDate): Boolean = date == LocalDate.now().minusDays(1)

    /**
     * Returns a relative label: "Today", "Yesterday", or formatted date.
     */
    fun relative(date: LocalDate): String = when {
        isToday(date)     -> "Today"
        isYesterday(date) -> "Yesterday"
        else              -> date.toDisplay()
    }
}

// ── Validation ────────────────────────────────────────────────────────────────

object Validators {

    fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPassword(password: String): Boolean =
        password.length >= 6

    fun isValidAmount(input: String): Boolean =
        input.toDoubleOrNull()?.let { it > 0 } ?: false

    fun isValidName(name: String): Boolean =
        name.trim().length >= 2
}

// ── Result Extensions ─────────────────────────────────────────────────────────

/**
 * Wraps a suspending block in a try/catch and returns Result.
 * Reduces boilerplate in repositories and use cases.
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try { Result.success(block()) }
    catch (e: Exception) { Result.failure(e) }
