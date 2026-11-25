package com.example

import com.example.model.TransactionCategory
import com.example.model.TransactionType
import com.example.ui.theme.ThemeMode
import com.example.util.formatCompactRupiah
import com.example.util.formatRupiah
import com.example.util.shiftYearMonth
import com.example.util.yearMonthFromMillis
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class FormattersAndModelsTest {

    @Test
    fun `formatRupiah formats positive and negative amounts`() {
        assertEquals("Rp 1.500.000", formatRupiah(1_500_000L))
        assertEquals("+Rp 250.000", formatRupiah(250_000L, includeSign = true))
        assertEquals("-Rp 45.000", formatRupiah(-45_000L, includeSign = true))
    }

    @Test
    fun `formatCompactRupiah abbreviates thousands and millions`() {
        assertEquals("Rp 500rb", formatCompactRupiah(500_000L))
        assertEquals("Rp 2,5jt", formatCompactRupiah(2_500_000L))
        assertEquals("Rp 1M", formatCompactRupiah(1_000_000_000L))
    }

    @Test
    fun `shiftYearMonth handles year boundaries correctly`() {
        assertEquals("2026-12", shiftYearMonth("2026-11", 1))
        assertEquals("2027-01", shiftYearMonth("2026-12", 1))
        assertEquals("2025-12", shiftYearMonth("2026-01", -1))
        assertEquals("2026-06", shiftYearMonth("2026-11", -5))
    }

    @Test
    fun `yearMonthFromMillis produces YYYY-MM`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 25, 12, 0, 0)
        }
        assertEquals("2026-11", yearMonthFromMillis(cal.timeInMillis))
    }

    @Test
    fun `safe enum fallbacks prevent unknown string crashes`() {
        assertEquals(TransactionType.EXPENSE, TransactionType.fromRaw("UNKNOWN_TYPE"))
        assertEquals(
            TransactionCategory.OTHER_INCOME,
            TransactionCategory.fromRaw("NON_EXISTENT", TransactionType.INCOME)
        )
        assertEquals(
            TransactionCategory.OTHER_EXPENSE,
            TransactionCategory.fromRaw("NON_EXISTENT", TransactionType.EXPENSE)
        )
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromRaw("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromRaw("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromRaw("INVALID_THEME"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromRaw(null))
    }
}
