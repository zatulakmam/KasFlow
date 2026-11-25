package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Transaction
import com.example.model.TransactionCategory
import com.example.model.TransactionType
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MinimalCard
import com.example.ui.components.MonthSelectorBar
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.LocalCashflowColors
import com.example.ui.viewmodel.CashflowDashboardState
import com.example.util.formatDateHeader
import com.example.util.formatRupiah

@Composable
fun TransactionsScreen(
    state: CashflowDashboardState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSearchChange: (String) -> Unit,
    onTypeFilterChange: (TransactionType?) -> Unit,
    onCategoryFilterChange: (TransactionCategory?) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalCashflowColors.current
    val grouped = remember(state.filteredTransactions) {
        state.filteredTransactions.groupBy { formatDateHeader(it.date) }
    }

    val categoryOptions = remember(state.filterType, state.monthTransactions) {
        state.monthTransactions
            .filter { state.filterType == null || it.parsedType == state.filterType }
            .map { it.parsedCategory }
            .distinct()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Transaksi",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Cari dan tinjau seluruh riwayat catatan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MonthSelectorBar(
                        monthLabel = state.selectedMonthLabel,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth
                    )
                }
            }

            item {
                MinimalCard(
                    containerColor = semantic.cardSurface,
                    borderColor = semantic.borderSubtle,
                    cornerRadius = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Cari judul, kategori, atau catatan...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Cari transaksi"
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Hapus pencarian"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = semantic.cardSurface,
                            unfocusedContainerColor = semantic.cardSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_transactions_input")
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.filterType == null,
                            onClick = { onTypeFilterChange(null) },
                            label = { Text("Semua (${state.monthTransactions.size})", fontWeight = FontWeight.SemiBold) },
                            border = BorderStroke(
                                1.dp,
                                if (state.filterType == null) semantic.primaryAction else semantic.borderSubtle
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = semantic.cardSurface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = semantic.primaryAction,
                                selectedLabelColor = semantic.onPrimaryAction
                            ),
                            modifier = Modifier.testTag("filter_type_all")
                        )
                        FilterChip(
                            selected = state.filterType == TransactionType.EXPENSE,
                            onClick = {
                                onTypeFilterChange(
                                    if (state.filterType == TransactionType.EXPENSE) null else TransactionType.EXPENSE
                                )
                            },
                            label = { Text("Pengeluaran", fontWeight = FontWeight.SemiBold) },
                            border = BorderStroke(
                                1.dp,
                                if (state.filterType == TransactionType.EXPENSE) semantic.expense else semantic.borderSubtle
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = semantic.cardSurface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = semantic.expenseContainer,
                                selectedLabelColor = semantic.expenseText
                            ),
                            modifier = Modifier.testTag("filter_type_expense")
                        )
                        FilterChip(
                            selected = state.filterType == TransactionType.INCOME,
                            onClick = {
                                onTypeFilterChange(
                                    if (state.filterType == TransactionType.INCOME) null else TransactionType.INCOME
                                )
                            },
                            label = { Text("Pemasukan", fontWeight = FontWeight.SemiBold) },
                            border = BorderStroke(
                                1.dp,
                                if (state.filterType == TransactionType.INCOME) semantic.income else semantic.borderSubtle
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = semantic.cardSurface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = semantic.incomeContainer,
                                selectedLabelColor = semantic.incomeText
                            ),
                            modifier = Modifier.testTag("filter_type_income")
                        )
                    }

                    if (categoryOptions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoryOptions.forEach { cat ->
                                val selected = state.filterCategory == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        onCategoryFilterChange(if (selected) null else cat)
                                    },
                                    label = { Text(cat.label, fontWeight = FontWeight.Medium) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) semantic.primaryAction else semantic.borderSubtle
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = semantic.cardSurface,
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        selectedContainerColor = semantic.primaryAction,
                                        selectedLabelColor = semantic.onPrimaryAction,
                                        selectedLeadingIconColor = semantic.onPrimaryAction
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (state.filteredTransactions.isNotEmpty()) {
                item {
                    val filteredIncome = state.filteredTransactions
                        .filter { it.parsedType == TransactionType.INCOME }
                        .sumOf { it.amount }
                    val filteredExpense = state.filteredTransactions
                        .filter { it.parsedType == TransactionType.EXPENSE }
                        .sumOf { it.amount }

                    MinimalCard(
                        containerColor = semantic.cardSurface,
                        borderColor = semantic.borderSubtle,
                        cornerRadius = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.filteredTransactions.size} transaksi",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (filteredIncome > 0L) {
                                    Text(
                                        text = "+${formatRupiah(filteredIncome)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = semantic.incomeText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (filteredExpense > 0L) {
                                    Text(
                                        text = "-${formatRupiah(filteredExpense)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = semantic.expenseText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (grouped.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.SearchOff,
                        title = "Tidak ada transaksi ditemukan",
                        subtitle = if (state.monthTransactions.isEmpty()) {
                            "Belum ada catatan untuk ${state.selectedMonthLabel}."
                        } else {
                            "Coba ubah kata kunci pencarian atau reset filter kategori."
                        }
                    )
                }
            } else {
                grouped.forEach { (dateLabel, itemsForDay) ->
                    item(key = "header_$dateLabel") {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }
                    items(
                        items = itemsForDay,
                        key = { it.id }
                    ) { tx ->
                        TransactionItemRow(
                            transaction = tx,
                            onClick = { onEditTransaction(tx) }
                        )
                    }
                }
            }
        }
    }
}
