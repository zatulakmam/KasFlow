package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TransactionRepository
import com.example.model.CategoryBreakdown
import com.example.model.DailyCashflowPoint
import com.example.model.MonthlyInsights
import com.example.model.MonthlyTrendPoint
import com.example.model.TransactionCategory
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.model.UiState
import com.example.util.currentYearMonth
import com.example.util.dayOfMonthFromMillis
import com.example.util.daysInYearMonth
import com.example.util.formatYearMonthFull
import com.example.util.formatYearMonthShort
import com.example.util.shiftYearMonth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CashflowDashboardState(
    val selectedYearMonth: String = currentYearMonth(),
    val selectedMonthLabel: String = formatYearMonthFull(currentYearMonth()),
    val overallNetBalance: Long = 0L,
    val monthIncome: Long = 0L,
    val monthExpense: Long = 0L,
    val monthNet: Long = 0L,
    val savingRatePercent: Int = 0,
    val monthTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val sixMonthTrend: List<MonthlyTrendPoint> = emptyList(),
    val dailyTrend: List<DailyCashflowPoint> = emptyList(),
    val expenseBreakdown: List<CategoryBreakdown> = emptyList(),
    val incomeBreakdown: List<CategoryBreakdown> = emptyList(),
    val insights: MonthlyInsights = MonthlyInsights(null, 0L, null, 0),
    val searchQuery: String = "",
    val filterType: TransactionType? = null,
    val filterCategory: TransactionCategory? = null,
    val totalSyncedCount: Int = 0
)

sealed interface ActionFeedback {
    data object Idle : ActionFeedback
    data object Saving : ActionFeedback
    data class Success(val message: String) : ActionFeedback
    data class Error(val message: String) : ActionFeedback
}

class CashflowViewModel(
    private val repository: TransactionRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedYearMonth = MutableStateFlow(currentYearMonth())
    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow<TransactionType?>(null)
    private val _filterCategory = MutableStateFlow<TransactionCategory?>(null)

    private val _actionFeedback = MutableStateFlow<ActionFeedback>(ActionFeedback.Idle)
    val actionFeedback: StateFlow<ActionFeedback> = _actionFeedback.asStateFlow()

    val transactionsState: StateFlow<UiState<List<Transaction>>> =
        repository.observeTransactions(currentUserId)
            .map<List<Transaction>, UiState<List<Transaction>>> { list ->
                UiState.Success(list)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing transactions", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "Gagal menyinkronkan data dari cloud."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val dashboardState: StateFlow<UiState<CashflowDashboardState>> = combine(
        transactionsState,
        _selectedYearMonth,
        _searchQuery,
        _filterType,
        _filterCategory
    ) { txUiState, selectedYm, query, typeFilter, categoryFilter ->
        when (txUiState) {
            is UiState.Loading -> UiState.Loading
            is UiState.Error -> UiState.Error(txUiState.message)
            is UiState.Success -> {
                UiState.Success(
                    computeDashboardState(
                        allTransactions = txUiState.data,
                        selectedYm = selectedYm,
                        query = query,
                        typeFilter = typeFilter,
                        categoryFilter = categoryFilter
                    )
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    fun previousMonth() {
        _selectedYearMonth.value = shiftYearMonth(_selectedYearMonth.value, -1)
    }

    fun nextMonth() {
        _selectedYearMonth.value = shiftYearMonth(_selectedYearMonth.value, +1)
    }

    fun selectYearMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType?) {
        _filterType.value = type
        if (type != null && _filterCategory.value?.type != type) {
            _filterCategory.value = null
        }
    }

    fun setCategoryFilter(category: TransactionCategory?) {
        _filterCategory.value = category
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _filterType.value = null
        _filterCategory.value = null
    }

    fun clearFeedback() {
        _actionFeedback.value = ActionFeedback.Idle
    }

    fun saveTransaction(
        editingId: String?,
        title: String,
        type: TransactionType,
        category: TransactionCategory,
        amount: Long,
        note: String,
        date: Long,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _actionFeedback.value = ActionFeedback.Saving
            val result = if (editingId.isNullOrBlank()) {
                repository.addTransaction(
                    title = title,
                    type = type,
                    category = category,
                    amount = amount,
                    note = note,
                    date = date
                )
            } else {
                repository.updateTransaction(
                    transactionId = editingId,
                    title = title,
                    type = type,
                    category = category,
                    amount = amount,
                    note = note,
                    date = date
                )
            }

            result.onSuccess {
                _actionFeedback.value = ActionFeedback.Success(
                    if (editingId.isNullOrBlank()) "Transaksi tersimpan & tersinkronisasi ke cloud"
                    else "Perubahan transaksi tersinkronisasi ke cloud"
                )
                onComplete()
            }.onFailure { err ->
                _actionFeedback.value = ActionFeedback.Error(
                    err.localizedMessage ?: "Gagal menyimpan transaksi ke cloud."
                )
            }
        }
    }

    fun deleteTransaction(transactionId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _actionFeedback.value = ActionFeedback.Saving
            repository.deleteTransaction(transactionId)
                .onSuccess {
                    _actionFeedback.value = ActionFeedback.Success("Transaksi dihapus dari cloud")
                    onComplete()
                }
                .onFailure { err ->
                    _actionFeedback.value = ActionFeedback.Error(
                        err.localizedMessage ?: "Gagal menghapus transaksi."
                    )
                }
        }
    }

    private fun computeDashboardState(
        allTransactions: List<Transaction>,
        selectedYm: String,
        query: String,
        typeFilter: TransactionType?,
        categoryFilter: TransactionCategory?
    ): CashflowDashboardState {
        var overallIncome = 0L
        var overallExpense = 0L
        allTransactions.forEach { tx ->
            when (tx.parsedType) {
                TransactionType.INCOME -> overallIncome += tx.amount
                TransactionType.EXPENSE -> overallExpense += tx.amount
            }
        }

        val monthTransactions = allTransactions.filter { it.yearMonth == selectedYm }
        val monthIncome = monthTransactions
            .filter { it.parsedType == TransactionType.INCOME }
            .sumOf { it.amount }
        val monthExpense = monthTransactions
            .filter { it.parsedType == TransactionType.EXPENSE }
            .sumOf { it.amount }
        val monthNet = monthIncome - monthExpense
        val savingRatePercent = if (monthIncome > 0L) {
            (((monthIncome - monthExpense).toDouble() / monthIncome.toDouble()) * 100.0)
                .toInt()
                .coerceIn(-999, 100)
        } else 0

        // 6-month trend ending at selectedYm
        val sixMonths = (-5..0).map { offset -> shiftYearMonth(selectedYm, offset) }
        val groupedByYm = allTransactions.groupBy { it.yearMonth }
        val sixMonthTrend = sixMonths.map { ym ->
            val txs = groupedByYm[ym].orEmpty()
            MonthlyTrendPoint(
                yearMonth = ym,
                shortLabel = formatYearMonthShort(ym),
                fullLabel = formatYearMonthFull(ym),
                income = txs.filter { it.parsedType == TransactionType.INCOME }.sumOf { it.amount },
                expense = txs.filter { it.parsedType == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }

        // Daily cashflow for selected month
        val daysCount = daysInYearMonth(selectedYm)
        val byDay = monthTransactions.groupBy { dayOfMonthFromMillis(it.date) }
        val dailyTrend = (1..daysCount).map { day ->
            val dayTxs = byDay[day].orEmpty()
            DailyCashflowPoint(
                dayOfMonth = day,
                income = dayTxs.filter { it.parsedType == TransactionType.INCOME }.sumOf { it.amount },
                expense = dayTxs.filter { it.parsedType == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }

        val expenseBreakdown = buildCategoryBreakdown(monthTransactions, TransactionType.EXPENSE, monthExpense)
        val incomeBreakdown = buildCategoryBreakdown(monthTransactions, TransactionType.INCOME, monthIncome)

        val activeExpenseDays = dailyTrend.filter { it.expense > 0L }
        val avgDailyExpense = if (activeExpenseDays.isNotEmpty()) {
            monthExpense / activeExpenseDays.size
        } else 0L
        val highestExpenseDay = activeExpenseDays.maxByOrNull { it.expense }

        val insights = MonthlyInsights(
            topExpenseCategory = expenseBreakdown.firstOrNull(),
            averageDailyExpense = avgDailyExpense,
            highestExpenseDay = highestExpenseDay,
            transactionCount = monthTransactions.size
        )

        val cleanQuery = query.trim().lowercase()
        val filtered = monthTransactions.filter { tx ->
            val matchesQuery = cleanQuery.isEmpty() ||
                tx.title.lowercase().contains(cleanQuery) ||
                tx.note.lowercase().contains(cleanQuery) ||
                tx.parsedCategory.label.lowercase().contains(cleanQuery)
            val matchesType = typeFilter == null || tx.parsedType == typeFilter
            val matchesCategory = categoryFilter == null || tx.parsedCategory == categoryFilter
            matchesQuery && matchesType && matchesCategory
        }

        return CashflowDashboardState(
            selectedYearMonth = selectedYm,
            selectedMonthLabel = formatYearMonthFull(selectedYm),
            overallNetBalance = overallIncome - overallExpense,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            monthNet = monthNet,
            savingRatePercent = savingRatePercent,
            monthTransactions = monthTransactions,
            filteredTransactions = filtered,
            sixMonthTrend = sixMonthTrend,
            dailyTrend = dailyTrend,
            expenseBreakdown = expenseBreakdown,
            incomeBreakdown = incomeBreakdown,
            insights = insights,
            searchQuery = query,
            filterType = typeFilter,
            filterCategory = categoryFilter,
            totalSyncedCount = allTransactions.size
        )
    }

    private fun buildCategoryBreakdown(
        transactions: List<Transaction>,
        type: TransactionType,
        totalForType: Long
    ): List<CategoryBreakdown> {
        return transactions
            .filter { it.parsedType == type }
            .groupBy { it.parsedCategory }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                val pct = if (totalForType > 0L) (sum.toFloat() / totalForType.toFloat()) else 0f
                CategoryBreakdown(
                    category = cat,
                    totalAmount = sum,
                    percentage = pct,
                    count = list.size
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    private companion object {
        const val TAG = "CashflowVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
