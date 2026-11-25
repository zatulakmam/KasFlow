package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Transaction
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MinimalButton
import com.example.ui.components.MinimalCard
import com.example.ui.components.MonthSelectorBar
import com.example.ui.components.MonthlyIncomeExpenseBarChart
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.LocalCashflowColors
import com.example.ui.viewmodel.CashflowDashboardState
import com.example.util.formatCompactRupiah
import com.example.util.formatRupiah

@Composable
fun DashboardScreen(
    state: CashflowDashboardState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (String) -> Unit,
    onAddTransaction: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenAllTransactions: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalCashflowColors.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWide = maxWidth >= 680.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Ringkasan kas",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Saldo, arus kas, dan catatan bulan ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MonthSelectorBar(
                            monthLabel = state.selectedMonthLabel,
                            onPreviousMonth = onPreviousMonth,
                            onNextMonth = onNextMonth,
                            modifier = Modifier.weight(1f)
                        )

                        MinimalButton(
                            onClick = onAddTransaction,
                            containerColor = semantic.primaryAction,
                            contentColor = semantic.onPrimaryAction,
                            cornerRadius = 12.dp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("header_add_transaction_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Catat",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TotalBalanceCard(
                            state = state,
                            modifier = Modifier.weight(1.1f)
                        )
                        StatCashflowCard(
                            label = "Pemasukan",
                            amount = formatRupiah(state.monthIncome),
                            subtitle = state.selectedMonthLabel,
                            badgeBg = semantic.incomeContainer,
                            badgeFg = semantic.incomeText,
                            icon = Icons.Rounded.ArrowDownward,
                            modifier = Modifier.weight(1f)
                        )
                        StatCashflowCard(
                            label = "Pengeluaran",
                            amount = formatRupiah(state.monthExpense),
                            subtitle = state.selectedMonthLabel,
                            badgeBg = semantic.expenseContainer,
                            badgeFg = semantic.expenseText,
                            icon = Icons.Rounded.ArrowUpward,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TotalBalanceCard(
                            state = state,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCashflowCard(
                                label = "Pemasukan",
                                amount = formatRupiah(state.monthIncome),
                                subtitle = state.selectedMonthLabel,
                                badgeBg = semantic.incomeContainer,
                                badgeFg = semantic.incomeText,
                                icon = Icons.Rounded.ArrowDownward,
                                modifier = Modifier.weight(1f)
                            )
                            StatCashflowCard(
                                label = "Pengeluaran",
                                amount = formatRupiah(state.monthExpense),
                                subtitle = state.selectedMonthLabel,
                                badgeBg = semantic.expenseContainer,
                                badgeFg = semantic.expenseText,
                                icon = Icons.Rounded.ArrowUpward,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                MinimalCard(
                    containerColor = semantic.cardSurface,
                    borderColor = semantic.borderSubtle,
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Arus kas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            MinimalButton(
                                onClick = onOpenAnalytics,
                                containerColor = semantic.subtleSurface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                borderColor = semantic.borderSubtle,
                                cornerRadius = 10.dp,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "6 bulan ›",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (state.sixMonthTrend.all { it.income == 0L && it.expense == 0L }) {
                            EmptyStateCard(
                                icon = Icons.Rounded.BarChart,
                                title = "Belum ada data grafik",
                                subtitle = "Catat pemasukan atau pengeluaran pertama untuk melihat grafik perbandingan bulanan."
                            )
                        } else {
                            MonthlyIncomeExpenseBarChart(
                                points = state.sixMonthTrend,
                                onMonthClick = { ym -> onSelectMonth(ym) }
                            )
                        }
                    }
                }
            }

            item {
                MinimalCard(
                    containerColor = semantic.cardSurface,
                    borderColor = semantic.borderSubtle,
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transaksi terbaru",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            MinimalButton(
                                onClick = onOpenAllTransactions,
                                containerColor = semantic.subtleSurface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                borderColor = semantic.borderSubtle,
                                cornerRadius = 10.dp,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Lihat semua",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (state.monthTransactions.isEmpty()) {
                            EmptyStateCard(
                                icon = Icons.Rounded.ReceiptLong,
                                title = "Belum ada catatan di ${state.selectedMonthLabel}",
                                subtitle = "Ketuk tombol + Catat untuk menambah catatan baru."
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                state.monthTransactions.take(4).forEach { tx ->
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

            if (state.expenseBreakdown.isNotEmpty()) {
                item {
                    MinimalCard(
                        containerColor = semantic.cardSurface,
                        borderColor = semantic.borderSubtle,
                        cornerRadius = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pengeluaran per kategori",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                MinimalButton(
                                    onClick = onOpenAnalytics,
                                    containerColor = semantic.subtleSurface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    borderColor = semantic.borderSubtle,
                                    cornerRadius = 10.dp,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Grafik",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            state.expenseBreakdown.take(4).forEach { item ->
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.category.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = formatCompactRupiah(item.totalAmount),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(semantic.subtleSurface)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(item.percentage.coerceIn(0.04f, 1f))
                                                .height(7.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(item.category.accentColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalBalanceCard(
    state: CashflowDashboardState,
    modifier: Modifier = Modifier
) {
    val semantic = LocalCashflowColors.current
    MinimalCard(
        containerColor = semantic.heroCardBg,
        borderColor = if (semantic.isDark) semantic.borderSubtle else semantic.heroCardBg,
        cornerRadius = 16.dp,
        modifier = modifier.testTag("hero_cashflow_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(semantic.heroCardContent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = semantic.heroCardContent,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "Saldo Total",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = semantic.heroCardContent.copy(alpha = 0.85f)
                )
            }

            Text(
                text = formatRupiah(state.overallNetBalance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = semantic.heroCardContent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Seluruh transaksi",
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.heroCardContent.copy(alpha = 0.72f)
                )
                if (state.monthIncome > 0L) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(semantic.heroCardContent.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Hemat ${state.savingRatePercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = semantic.heroCardContent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCashflowCard(
    label: String,
    amount: String,
    subtitle: String,
    badgeBg: Color,
    badgeFg: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val semantic = LocalCashflowColors.current
    MinimalCard(
        containerColor = semantic.cardSurface,
        borderColor = semantic.borderSubtle,
        cornerRadius = 16.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = badgeFg,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
