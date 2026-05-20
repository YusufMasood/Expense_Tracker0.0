package com.yusuf.expensepro.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Double.formatAmount(): String {
    return if (this == kotlin.math.floor(this)) {
        "₹%,.0f".format(this)
    } else {
        "₹%,.2f".format(this)
    }
}

fun LocalDate.formatDate(): String =
    this.format(DateTimeFormatter.ofPattern("d MMM yyyy"))

fun LocalDate.formatMonthYear(): String =
    this.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
