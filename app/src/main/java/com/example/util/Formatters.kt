package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private val indonesianLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()

fun formatRupiah(amount: Long, includeSign: Boolean = false): String {
    val formatter = NumberFormat.getNumberInstance(indonesianLocale)
    val absFormatted = formatter.format(abs(amount))
    return when {
        amount < 0 -> "-Rp $absFormatted"
        includeSign && amount > 0 -> "+Rp $absFormatted"
        else -> "Rp $absFormatted"
    }
}

fun formatCompactRupiah(amount: Long): String {
    val absVal = abs(amount)
    val sign = if (amount < 0) "-" else ""
    return when {
        absVal >= 1_000_000_000L -> "${sign}Rp ${trimDecimal(absVal / 1_000_000_000.0)}M"
        absVal >= 1_000_000L -> "${sign}Rp ${trimDecimal(absVal / 1_000_000.0)}jt"
        absVal >= 1_000L -> "${sign}Rp ${trimDecimal(absVal / 1_000.0)}rb"
        else -> "${sign}Rp $absVal"
    }
}

private fun trimDecimal(value: Double): String {
    val rounded = ((value * 10.0).toLong()) / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        rounded.toString().replace('.', ',')
    }
}

fun currentYearMonth(): String = yearMonthFromMillis(System.currentTimeMillis())

fun yearMonthFromMillis(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    return String.format(Locale.US, "%04d-%02d", year, month)
}

fun shiftYearMonth(yearMonth: String, deltaMonths: Int): String {
    val (year, month) = parseYearMonth(yearMonth)
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 15)
        add(Calendar.MONTH, deltaMonths)
    }
    return String.format(
        Locale.US,
        "%04d-%02d",
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1
    )
}

fun parseYearMonth(yearMonth: String): Pair<Int, Int> {
    val parts = yearMonth.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1).coerceIn(1, 12)
    return year to month
}

fun daysInYearMonth(yearMonth: String): Int {
    val (year, month) = parseYearMonth(yearMonth)
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun dayOfMonthFromMillis(millis: Long): Int {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return cal.get(Calendar.DAY_OF_MONTH)
}

fun formatYearMonthFull(yearMonth: String): String {
    val (year, month) = parseYearMonth(yearMonth)
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val fmt = SimpleDateFormat("MMMM yyyy", indonesianLocale)
    return fmt.format(cal.time).replaceFirstChar { it.uppercase(indonesianLocale) }
}

fun formatYearMonthShort(yearMonth: String): String {
    val (year, month) = parseYearMonth(yearMonth)
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val fmt = SimpleDateFormat("MMM", indonesianLocale)
    return fmt.format(cal.time).replaceFirstChar { it.uppercase(indonesianLocale) }
}

fun formatDateHeader(millis: Long): String {
    val target = Calendar.getInstance().apply { timeInMillis = millis }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        isSameDay(target, today) -> "Hari Ini"
        isSameDay(target, yesterday) -> "Kemarin"
        else -> {
            val fmt = SimpleDateFormat("EEEE, d MMM yyyy", indonesianLocale)
            fmt.format(Date(millis)).replaceFirstChar { it.uppercase(indonesianLocale) }
        }
    }
}

fun formatDateShort(millis: Long): String {
    val fmt = SimpleDateFormat("d MMM yyyy", indonesianLocale)
    return fmt.format(Date(millis))
}

fun defaultMillisForYearMonth(yearMonth: String): Long {
    val currentYm = currentYearMonth()
    if (yearMonth == currentYm) return System.currentTimeMillis()
    val (year, month) = parseYearMonth(yearMonth)
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
