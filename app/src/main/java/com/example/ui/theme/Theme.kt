package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class CashflowSemanticColors(
    val isDark: Boolean,
    val canvasBg: Color,
    val cardSurface: Color,
    val barSurface: Color,
    val subtleSurface: Color,
    val borderSubtle: Color,
    val primaryAction: Color,
    val onPrimaryAction: Color,
    val heroCardBg: Color,
    val heroCardContent: Color,
    val income: Color,
    val incomeContainer: Color,
    val incomeText: Color,
    val expense: Color,
    val expenseContainer: Color,
    val expenseText: Color,
    val mintBadge: Color,
    val mintText: Color,
    val trendLine: Color
)

private val LightSemanticColors = CashflowSemanticColors(
    isDark = false,
    canvasBg = LightCanvas,
    cardSurface = LightCard,
    barSurface = LightBar,
    subtleSurface = LightSubtle,
    borderSubtle = LightBorder,
    primaryAction = NavyPrimary,
    onPrimaryAction = Color.White,
    heroCardBg = NavyPrimary,
    heroCardContent = Color.White,
    income = LightIncome,
    incomeContainer = LightIncomeContainer,
    incomeText = LightIncomeText,
    expense = LightExpense,
    expenseContainer = LightExpenseContainer,
    expenseText = LightExpenseText,
    mintBadge = LightMintBadge,
    mintText = LightMintText,
    trendLine = NavyPrimary
)

private val DarkSemanticColors = CashflowSemanticColors(
    isDark = true,
    canvasBg = DarkCanvas,
    cardSurface = DarkCard,
    barSurface = DarkBar,
    subtleSurface = DarkSubtle,
    borderSubtle = DarkBorder,
    primaryAction = DarkPrimary,
    onPrimaryAction = DarkBar,
    heroCardBg = DarkHeroNavy,
    heroCardContent = DarkTextPrimary,
    income = DarkIncome,
    incomeContainer = DarkIncomeContainer,
    incomeText = DarkIncomeText,
    expense = DarkExpense,
    expenseContainer = DarkExpenseContainer,
    expenseText = DarkExpenseText,
    mintBadge = DarkMintBadge,
    mintText = DarkMintText,
    trendLine = DarkIncome
)

val LocalCashflowColors = staticCompositionLocalOf { LightSemanticColors }

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSubtle,
    onPrimaryContainer = NavyPrimary,
    secondary = LightIncome,
    onSecondary = Color.White,
    secondaryContainer = LightIncomeContainer,
    onSecondaryContainer = LightIncomeText,
    tertiary = LightExpense,
    onTertiary = Color.White,
    tertiaryContainer = LightExpenseContainer,
    onTertiaryContainer = LightExpenseText,
    background = LightCanvas,
    onBackground = SlateTextPrimary,
    surface = LightCard,
    onSurface = SlateTextPrimary,
    surfaceVariant = LightSubtle,
    onSurfaceVariant = SlateTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = LightExpenseContainer,
    onErrorContainer = LightExpenseText
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkBar,
    primaryContainer = DarkSubtle,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkIncome,
    onSecondary = DarkBar,
    secondaryContainer = DarkIncomeContainer,
    onSecondaryContainer = DarkIncomeText,
    tertiary = DarkExpense,
    onTertiary = DarkBar,
    tertiaryContainer = DarkExpenseContainer,
    onTertiaryContainer = DarkExpenseText,
    background = DarkCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSubtle,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkExpense,
    onError = DarkBar,
    errorContainer = DarkExpenseContainer,
    onErrorContainer = DarkExpenseText
)

private val KasFlowShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun KasFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(LocalCashflowColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KasFlowTypography,
            shapes = KasFlowShapes,
            content = content
        )
    }
}
