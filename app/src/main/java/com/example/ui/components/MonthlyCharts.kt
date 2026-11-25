package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryBreakdown
import com.example.model.DailyCashflowPoint
import com.example.model.MonthlyTrendPoint
import com.example.ui.theme.LocalCashflowColors
import com.example.util.formatCompactRupiah
import com.example.util.formatRupiah
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun MonthlyIncomeExpenseBarChart(
    points: List<MonthlyTrendPoint>,
    modifier: Modifier = Modifier,
    onMonthClick: ((String) -> Unit)? = null
) {
    val semantic = LocalCashflowColors.current
    val gridColor = semantic.borderSubtle
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeMonthColor = MaterialTheme.colorScheme.onSurface
    val selectedHighlight = semantic.subtleSurface
    val textMeasurer = rememberTextMeasurer()

    var selectedIndex by remember(points) {
        mutableIntStateOf(points.lastIndex.coerceAtLeast(0))
    }

    val maxVal = remember(points) {
        val peak = points.maxOfOrNull { max(it.income, it.expense) } ?: 0L
        if (peak <= 0L) 100_000L else (peak * 1.15).toLong()
    }

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bar_chart_progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MinimalLegendDot(
                    color = semantic.income,
                    label = "Pemasukan"
                )
                MinimalLegendDot(
                    color = semantic.expense,
                    label = "Pengeluaran"
                )
            }
            Text(
                text = "Ketuk batang",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val selectedPoint = points.getOrNull(selectedIndex)
        if (selectedPoint != null) {
            AnimatedContent(
                targetState = selectedPoint,
                label = "selected_month_callout"
            ) { point ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(semantic.subtleSurface)
                        .border(1.dp, semantic.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = point.fullLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Selisih: ${formatRupiah(point.net, includeSign = true)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (point.net >= 0) semantic.incomeText else semantic.expenseText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${formatCompactRupiah(point.income)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = semantic.incomeText,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "-${formatCompactRupiah(point.expense)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = semantic.expenseText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .testTag("monthly_bar_chart")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            if (points.isEmpty()) return@detectTapGestures
                            val leftMargin = 54.dp.toPx()
                            val chartWidth = (size.width - leftMargin).coerceAtLeast(1f)
                            val groupWidth = chartWidth / points.size
                            val idx = ((offset.x - leftMargin) / groupWidth)
                                .toInt()
                                .coerceIn(0, points.lastIndex)
                            selectedIndex = idx
                            onMonthClick?.invoke(points[idx].yearMonth)
                        }
                    }
            ) {
                if (points.isEmpty()) return@Canvas

                val leftMargin = 54.dp.toPx()
                val topMargin = 10.dp.toPx()
                val bottomMargin = 28.dp.toPx()
                val chartWidth = (size.width - leftMargin).coerceAtLeast(1f)
                val chartHeight = (size.height - topMargin - bottomMargin).coerceAtLeast(1f)

                val gridSteps = 3
                val axisStyle = TextStyle(
                    color = labelColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                for (step in 0..gridSteps) {
                    val ratio = step.toFloat() / gridSteps.toFloat()
                    val y = topMargin + chartHeight * (1f - ratio)
                    drawLine(
                        color = gridColor,
                        start = Offset(leftMargin, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = if (step == 0) null else dashEffect
                    )
                    val tickAmount = (maxVal * ratio).toLong()
                    val tickText = if (step == 0) "0" else formatCompactRupiah(tickAmount).removePrefix("Rp ")
                    val measured = textMeasurer.measure(tickText, style = axisStyle)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            x = (leftMargin - measured.size.width - 8.dp.toPx()).coerceAtLeast(0f),
                            y = (y - measured.size.height / 2f).coerceIn(0f, size.height - measured.size.height)
                        )
                    )
                }

                val groupWidth = chartWidth / points.size
                val barWidth = (groupWidth * 0.27f).coerceIn(8.dp.toPx(), 18.dp.toPx())
                val barGap = 4.dp.toPx()

                points.forEachIndexed { index, point ->
                    val groupStart = leftMargin + index * groupWidth
                    val groupCenter = groupStart + groupWidth / 2f

                    if (index == selectedIndex) {
                        drawRoundRect(
                            color = selectedHighlight,
                            topLeft = Offset(groupStart + 2.dp.toPx(), topMargin),
                            size = Size(groupWidth - 4.dp.toPx(), chartHeight),
                            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                    }

                    val incomeRatio = (point.income.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                    val expenseRatio = (point.expense.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)

                    val incomeHeight = (chartHeight * incomeRatio * animProgress)
                        .let { if (point.income > 0) max(it, 5.dp.toPx()) else 0f }
                    val expenseHeight = (chartHeight * expenseRatio * animProgress)
                        .let { if (point.expense > 0) max(it, 5.dp.toPx()) else 0f }

                    val incomeX = groupCenter - barGap / 2f - barWidth
                    val expenseX = groupCenter + barGap / 2f
                    val baseLineY = topMargin + chartHeight
                    val cornerR = CornerRadius(5.dp.toPx(), 5.dp.toPx())

                    if (incomeHeight > 0f) {
                        drawRoundRect(
                            color = semantic.income,
                            topLeft = Offset(incomeX, baseLineY - incomeHeight),
                            size = Size(barWidth, incomeHeight),
                            cornerRadius = cornerR
                        )
                    }

                    if (expenseHeight > 0f) {
                        drawRoundRect(
                            color = semantic.expense,
                            topLeft = Offset(expenseX, baseLineY - expenseHeight),
                            size = Size(barWidth, expenseHeight),
                            cornerRadius = cornerR
                        )
                    }

                    val monthStyle = TextStyle(
                        color = if (index == selectedIndex) activeMonthColor else labelColor,
                        fontSize = 11.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Medium
                    )
                    val labelLayout = textMeasurer.measure(point.shortLabel, style = monthStyle)
                    drawText(
                        textLayoutResult = labelLayout,
                        topLeft = Offset(
                            x = groupCenter - labelLayout.size.width / 2f,
                            y = baseLineY + 7.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyNetTrendChart(
    points: List<MonthlyTrendPoint>,
    modifier: Modifier = Modifier
) {
    val semantic = LocalCashflowColors.current
    val gridColor = semantic.borderSubtle
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeMonthColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()

    var selectedIndex by remember(points) {
        mutableIntStateOf(points.lastIndex.coerceAtLeast(0))
    }

    val (minVal, maxVal) = remember(points) {
        val nets = points.map { it.net }
        val minN = (nets.minOrNull() ?: 0L).coerceAtMost(0L)
        val maxN = (nets.maxOrNull() ?: 0L).coerceAtLeast(100_000L)
        val span = (maxN - minN).coerceAtLeast(100_000L)
        val pad = (span * 0.15).toLong()
        (minN - (if (minN < 0) pad else 0L)) to (maxN + pad)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        val selected = points.getOrNull(selectedIndex)
        if (selected != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Arus bersih ${selected.fullLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatRupiah(selected.net, includeSign = true),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected.net >= 0) semantic.incomeText else semantic.expenseText
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .testTag("monthly_net_trend_chart")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            if (points.isEmpty()) return@detectTapGestures
                            val leftMargin = 54.dp.toPx()
                            val rightMargin = 14.dp.toPx()
                            val chartWidth = (size.width - leftMargin - rightMargin).coerceAtLeast(1f)
                            val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth
                            val idx = ((offset.x - leftMargin) / stepX)
                                .roundToInt()
                                .coerceIn(0, points.lastIndex)
                            selectedIndex = idx
                        }
                    }
            ) {
                if (points.isEmpty()) return@Canvas
                val leftMargin = 54.dp.toPx()
                val rightMargin = 14.dp.toPx()
                val topMargin = 12.dp.toPx()
                val bottomMargin = 26.dp.toPx()
                val chartWidth = (size.width - leftMargin - rightMargin).coerceAtLeast(1f)
                val chartHeight = (size.height - topMargin - bottomMargin).coerceAtLeast(1f)
                val range = (maxVal - minVal).coerceAtLeast(1L).toFloat()

                fun yFor(value: Long): Float {
                    val norm = (value - minVal).toFloat() / range
                    return topMargin + chartHeight * (1f - norm)
                }

                val zeroY = yFor(0L).coerceIn(topMargin, topMargin + chartHeight)
                drawLine(
                    color = gridColor,
                    start = Offset(leftMargin, zeroY),
                    end = Offset(size.width - rightMargin, zeroY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                val axisStyle = TextStyle(color = labelColor, fontSize = 10.sp)
                val topLabel = textMeasurer.measure(formatCompactRupiah(maxVal).removePrefix("Rp "), axisStyle)
                drawText(topLabel, topLeft = Offset((leftMargin - topLabel.size.width - 8.dp.toPx()).coerceAtLeast(0f), topMargin - 4.dp.toPx()))
                val zeroLabel = textMeasurer.measure("0", axisStyle)
                drawText(zeroLabel, topLeft = Offset((leftMargin - zeroLabel.size.width - 8.dp.toPx()).coerceAtLeast(0f), zeroY - zeroLabel.size.height / 2f))

                val coords = points.mapIndexed { idx, pt ->
                    val x = if (points.size == 1) {
                        leftMargin + chartWidth / 2f
                    } else {
                        leftMargin + (chartWidth * idx / (points.size - 1).toFloat())
                    }
                    Offset(x, yFor(pt.net))
                }

                if (coords.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.lastIndex) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val cx = (p0.x + p1.x) / 2f
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, topMargin + chartHeight)
                        lineTo(coords.first().x, topMargin + chartHeight)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                semantic.trendLine.copy(alpha = 0.18f),
                                semantic.trendLine.copy(alpha = 0.01f)
                            ),
                            startY = topMargin,
                            endY = topMargin + chartHeight
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = semantic.trendLine,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                val baseLineY = topMargin + chartHeight
                coords.forEachIndexed { idx, pt ->
                    val isSel = idx == selectedIndex
                    val ptColor = if (points[idx].net >= 0) semantic.income else semantic.expense
                    val radius = if (isSel) 6.dp.toPx() else 4.dp.toPx()
                    drawCircle(color = semantic.cardSurface, radius = radius + 1.5.dp.toPx(), center = pt)
                    drawCircle(color = ptColor, radius = radius, center = pt)

                    val lbl = textMeasurer.measure(
                        points[idx].shortLabel,
                        TextStyle(
                            color = if (isSel) activeMonthColor else labelColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    drawText(lbl, topLeft = Offset(pt.x - lbl.size.width / 2f, baseLineY + 6.dp.toPx()))
                }
            }
        }
    }
}

@Composable
fun CategoryDonutChart(
    items: List<CategoryBreakdown>,
    totalAmount: Long,
    centerTitle: String,
    modifier: Modifier = Modifier
) {
    val semantic = LocalCashflowColors.current
    var selectedIdx by remember(items) { mutableIntStateOf(0) }
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "donut_progress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(204.dp)
                .testTag("category_donut_chart"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(items) {
                        detectTapGestures { offset ->
                            if (items.isEmpty()) return@detectTapGestures
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val dx = offset.x - cx
                            val dy = offset.y - cy
                            val dist = hypot(dx, dy)
                            val minDim = minOf(size.width, size.height).toFloat()
                            if (dist < minDim * 0.22f) return@detectTapGestures
                            var angle = (atan2(dy, dx) * 180.0 / PI).toFloat() + 90f
                            if (angle < 0f) angle += 360f
                            var acc = 0f
                            items.forEachIndexed { idx, item ->
                                val sweep = item.percentage * 360f
                                if (angle in acc..(acc + sweep)) {
                                    selectedIdx = idx
                                    return@detectTapGestures
                                }
                                acc += sweep
                            }
                        }
                    }
            ) {
                val strokeBase = 24.dp.toPx()
                val diameter = size.minDimension - strokeBase - 14.dp.toPx()
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                if (items.isEmpty() || totalAmount <= 0L) {
                    drawArc(
                        color = semantic.borderSubtle,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeBase, cap = StrokeCap.Round)
                    )
                    return@Canvas
                }

                var startAngle = -90f
                val gapDegrees = if (items.size > 1) 3.5f else 0f
                items.forEachIndexed { index, item ->
                    val rawSweep = (item.percentage * 360f) * animProgress
                    val sweep = (rawSweep - gapDegrees).coerceAtLeast(1f)
                    val isSelected = index == selectedIdx
                    val widthPx = if (isSelected) strokeBase + 6.dp.toPx() else strokeBase
                    drawArc(
                        color = item.category.accentColor,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = widthPx, cap = StrokeCap.Butt)
                    )
                    startAngle += rawSweep
                }
            }

            val focused = items.getOrNull(selectedIdx)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 28.dp)
            ) {
                Text(
                    text = focused?.category?.label ?: centerTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCompactRupiah(focused?.totalAmount ?: totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (focused != null) {
                    Text(
                        text = "${(focused.percentage * 100f).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEachIndexed { idx, item ->
                val isSelected = idx == selectedIdx
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) semantic.subtleSurface
                            else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) semantic.borderSubtle else semantic.borderSubtle.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedIdx = idx }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(item.category.accentColor)
                                )
                                Text(
                                    text = item.category.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "(${item.count}x)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatRupiah(item.totalAmount),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(semantic.subtleSurface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(item.percentage.coerceIn(0.03f, 1f))
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(item.category.accentColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyCashflowBarChart(
    days: List<DailyCashflowPoint>,
    modifier: Modifier = Modifier
) {
    val semantic = LocalCashflowColors.current
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    val maxVal = remember(days) {
        val peak = days.maxOfOrNull { max(it.income, it.expense) } ?: 0L
        if (peak <= 0L) 100_000L else peak
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(145.dp)
            .testTag("daily_cashflow_chart")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (days.isEmpty()) return@Canvas
            val topMargin = 8.dp.toPx()
            val bottomMargin = 22.dp.toPx()
            val chartHeight = (size.height - topMargin - bottomMargin).coerceAtLeast(1f)
            val slotWidth = size.width / days.size
            val barWidth = (slotWidth * 0.65f).coerceIn(3.dp.toPx(), 10.dp.toPx())
            val baseLineY = topMargin + chartHeight

            drawLine(
                color = semantic.borderSubtle,
                start = Offset(0f, baseLineY),
                end = Offset(size.width, baseLineY),
                strokeWidth = 1.dp.toPx()
            )

            days.forEachIndexed { idx, pt ->
                val centerX = idx * slotWidth + slotWidth / 2f
                val expH = (chartHeight * (pt.expense.toFloat() / maxVal.toFloat())).coerceIn(0f, chartHeight)
                val incH = (chartHeight * (pt.income.toFloat() / maxVal.toFloat())).coerceIn(0f, chartHeight)

                if (incH > 0f) {
                    val h = max(incH, 4.dp.toPx())
                    drawRoundRect(
                        color = semantic.income,
                        topLeft = Offset(centerX - barWidth / 2f, baseLineY - h),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
                if (expH > 0f) {
                    val h = max(expH, 4.dp.toPx())
                    drawRoundRect(
                        color = semantic.expense,
                        topLeft = Offset(centerX - barWidth / 2f, baseLineY - h),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }

                if (pt.dayOfMonth == 1 || pt.dayOfMonth % 5 == 0 || pt.dayOfMonth == days.size) {
                    val lbl = textMeasurer.measure(
                        pt.dayOfMonth.toString(),
                        TextStyle(color = labelColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    )
                    drawText(
                        lbl,
                        topLeft = Offset(
                            x = (centerX - lbl.size.width / 2f).coerceIn(0f, size.width - lbl.size.width),
                            y = baseLineY + 5.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalLegendDot(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
