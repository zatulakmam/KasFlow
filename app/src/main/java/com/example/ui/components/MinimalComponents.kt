package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalCashflowColors

@Composable
fun MinimalCard(
    modifier: Modifier = Modifier,
    containerColor: Color = LocalCashflowColors.current.cardSurface,
    borderColor: Color = LocalCashflowColors.current.borderSubtle,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = if (onClick != null) {
            modifier
                .clip(shape)
                .clickable(onClick = onClick)
        } else {
            modifier
        },
        shape = shape,
        color = containerColor,
        border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(content = content)
    }
}

@Composable
fun MinimalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = LocalCashflowColors.current.primaryAction,
    contentColor: Color = LocalCashflowColors.current.onPrimaryAction,
    borderColor: Color? = null,
    cornerRadius: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val effectiveBg = if (enabled) containerColor else containerColor.copy(alpha = 0.45f)
    val effectiveFg = if (enabled) contentColor else contentColor.copy(alpha = 0.55f)
    val shape = RoundedCornerShape(cornerRadius)

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        shape = shape,
        color = effectiveBg,
        contentColor = effectiveFg,
        border = borderColor?.let {
            BorderStroke(1.dp, if (enabled) it else it.copy(alpha = 0.45f))
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        CompositionLocalProvider(LocalContentColor provides effectiveFg) {
            Row(
                modifier = Modifier.padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun MinimalPillBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun <T> MinimalSegmentedTabs(
    options: List<T>,
    selectedOption: T,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "segment"
) {
    val semantic = LocalCashflowColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(semantic.subtleSurface)
            .border(1.dp, semantic.borderSubtle, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val selected = option == selectedOption
            val bg = if (selected) semantic.cardSurface else Color.Transparent
            val fg = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 40.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(bg)
                    .then(
                        if (selected) {
                            Modifier.border(1.dp, semantic.borderSubtle, RoundedCornerShape(9.dp))
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("${testTagPrefix}_${option.toString().lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelFor(option),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BrandRpBadge(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val semantic = LocalCashflowColors.current
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(semantic.heroCardBg)
            .then(
                if (semantic.isDark) {
                    Modifier.border(1.dp, semantic.borderSubtle, RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Rp",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = semantic.heroCardContent
        )
    }
}

@Composable
fun ThemeToggleButton(
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalCashflowColors.current
    val isDark = semantic.isDark
    val icon = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode
    val description = if (isDark) "Beralih ke mode terang" else "Beralih ke mode gelap"

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(onClick = onToggle)
            .testTag("theme_toggle_button"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(semantic.subtleSurface)
                .border(1.dp, semantic.borderSubtle, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
