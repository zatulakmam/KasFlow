package com.example.model

import androidx.annotation.Keep
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TransactionType(val label: String) {
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran");

    companion object {
        fun fromRaw(raw: String?): TransactionType =
            entries.firstOrNull { it.name == raw } ?: EXPENSE
    }
}

enum class TransactionCategory(
    val label: String,
    val type: TransactionType,
    val accentHex: Long,
    val icon: ImageVector
) {
    // Pengeluaran (Expense)
    FOOD("Makanan & Minuman", TransactionType.EXPENSE, 0xFFE67E22, Icons.Rounded.Restaurant),
    TRANSPORT("Transportasi", TransactionType.EXPENSE, 0xFF2980B9, Icons.Rounded.DirectionsCar),
    SHOPPING("Belanja", TransactionType.EXPENSE, 0xFF8E44AD, Icons.Rounded.ShoppingBag),
    BILLS("Tagihan & Utilitas", TransactionType.EXPENSE, 0xFFD64545, Icons.Rounded.ReceiptLong),
    ENTERTAINMENT("Hiburan", TransactionType.EXPENSE, 0xFFE84393, Icons.Rounded.Movie),
    HEALTH("Kesehatan", TransactionType.EXPENSE, 0xFF16A085, Icons.Rounded.Favorite),
    EDUCATION("Pendidikan", TransactionType.EXPENSE, 0xFF3F51B5, Icons.Rounded.School),
    OTHER_EXPENSE("Lainnya", TransactionType.EXPENSE, 0xFF607D8B, Icons.Rounded.MoreHoriz),

    // Pemasukan (Income)
    SALARY("Gaji", TransactionType.INCOME, 0xFF0E9F6E, Icons.Rounded.Payments),
    BONUS("Bonus & THR", TransactionType.INCOME, 0xFFD97706, Icons.Rounded.Stars),
    BUSINESS("Usaha & Freelance", TransactionType.INCOME, 0xFF0284C7, Icons.Rounded.Work),
    INVESTMENT("Investasi", TransactionType.INCOME, 0xFF7C3AED, Icons.Rounded.TrendingUp),
    GIFT("Hadiah", TransactionType.INCOME, 0xFFDB2777, Icons.Rounded.CardGiftcard),
    OTHER_INCOME("Lainnya", TransactionType.INCOME, 0xFF475569, Icons.Rounded.AccountBalanceWallet);

    val accentColor: Color
        get() = Color(accentHex)

    companion object {
        fun forType(type: TransactionType): List<TransactionCategory> =
            entries.filter { it.type == type }

        fun fromRaw(raw: String?, fallbackType: TransactionType): TransactionCategory =
            entries.firstOrNull { it.name == raw }
                ?: if (fallbackType == TransactionType.INCOME) OTHER_INCOME else OTHER_EXPENSE
    }
}

@Keep
@IgnoreExtraProperties
data class Transaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val amount: Long = 0L,
    val type: String = TransactionType.EXPENSE.name,
    val category: String = TransactionCategory.FOOD.name,
    val date: Long = 0L,
    val title: String = "",
    val note: String = "",
    val yearMonth: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedType: TransactionType
        get() = TransactionType.fromRaw(type)

    val parsedCategory: TransactionCategory
        get() = TransactionCategory.fromRaw(category, parsedType)
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class MonthlyTrendPoint(
    val yearMonth: String,
    val shortLabel: String,
    val fullLabel: String,
    val income: Long,
    val expense: Long
) {
    val net: Long get() = income - expense
}

data class DailyCashflowPoint(
    val dayOfMonth: Int,
    val income: Long,
    val expense: Long
)

data class CategoryBreakdown(
    val category: TransactionCategory,
    val totalAmount: Long,
    val percentage: Float,
    val count: Int
)

data class MonthlyInsights(
    val topExpenseCategory: CategoryBreakdown?,
    val averageDailyExpense: Long,
    val highestExpenseDay: DailyCashflowPoint?,
    val transactionCount: Int
)
