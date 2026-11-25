package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.TransactionType
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.DailyCashflowBarChart
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MinimalCard
import com.example.ui.components.MinimalSegmentedTabs
import com.example.ui.components.MonthSelectorBar
import com.example.ui.components.MonthlyIncomeExpenseBarChart
import com.example.ui.components.MonthlyNetTrendChart
import com.example.ui.theme.LocalCashflowColors
import com.example.ui.viewmodel.CashflowDashboardState
import com.example.util.formatCompactRupiah
import com.example.util.formatRupiah

@Composable
fun AnalyticsScreen(
    state: CashflowDashboardState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryChartType by remember { mutableStateOf(TransactionType.EXPENSE) }
    val semantic = LocalCashflowColors.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
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
                            text = "Laporan bulanan",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Grafik perbandingan, tren bersih, dan kategori.",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsightMetricCard(
                        icon = Icons.Rounded.Insights,
                        badgeBg = semantic.incomeContainer,
                        badgeFg = semantic.incomeText,
                        title = "Rata-rata harian",
                        value = formatCompactRupiah(state.insights.averageDailyExpense),
                        subtitle = "Per hari aktif keluar",
                        modifier = Modifier.weight(1f)
                    )
                    InsightMetricCard(
                        icon = Icons.Rounded.CalendarToday,
                        badgeBg = semantic.expenseContainer,
                        badgeFg = semantic.expenseText,
                        title = "Puncak keluar",
                        value = state.insights.highestExpenseDay?.let {
                            formatCompactRupiah(it.expense)
                        } ?: "Rp 0",
                        subtitle = state.insights.highestExpenseDay?.let {
                            "Tanggal ${it.dayOfMonth}"
                        } ?: "Belum ada data",
                        modifier = Modifier.weight(1f)
                    )
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Arus kas 6 bulan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        MonthlyIncomeExpenseBarChart(
                            points = state.sixMonthTrend,
                            onMonthClick = onSelectMonth
                        )
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
                                    .background(semantic.subtleSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TrendingUp,
                                    contentDescription = null,
                                    tint = semantic.trendLine,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Tren saldo bersih",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        MonthlyNetTrendChart(points = state.sixMonthTrend)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Aktivitas harian (${state.selectedMonthLabel})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Biru untuk pemasukan, merah muda untuk pengeluaran.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DailyCashflowBarChart(days = state.dailyTrend)
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(semantic.incomeContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DonutLarge,
                                    contentDescription = null,
                                    tint = semantic.incomeText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Rincian per kategori",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        MinimalSegmentedTabs(
                            options = listOf(TransactionType.EXPENSE, TransactionType.INCOME),
                            selectedOption = categoryChartType,
                            labelFor = { it.label },
                            onSelect = { categoryChartType = it },
                            testTagPrefix = "donut_toggle"
                        )

                        val activeList = if (categoryChartType == TransactionType.EXPENSE) {
                            state.expenseBreakdown
                        } else {
                            state.incomeBreakdown
                        }
                        val totalForType = if (categoryChartType == TransactionType.EXPENSE) {
                            state.monthExpense
                        } else {
                            state.monthIncome
                        }

                        if (activeList.isEmpty()) {
                            EmptyStateCard(
                                icon = Icons.Rounded.DonutLarge,
                                title = "Belum ada ${categoryChartType.label.lowercase()}",
                                subtitle = "Tambahkan transaksi di ${state.selectedMonthLabel} untuk melihat komposisi kategori."
                            )
                        } else {
                            CategoryDonutChart(
                                items = activeList,
                                totalAmount = totalForType,
                                centerTitle = "Total ${categoryChartType.label}"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total ${categoryChartType.label}: ${formatRupiah(totalForType)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (categoryChartType == TransactionType.EXPENSE) {
                                    semantic.expenseText
                                } else {
                                    semantic.incomeText
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightMetricCard(
    icon: ImageVector,
    badgeBg: Color,
    badgeFg: Color,
    title: String,
    value: String,
    subtitle: String,
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
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeFg,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
