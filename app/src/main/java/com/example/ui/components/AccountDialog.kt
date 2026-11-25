package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.LocalCashflowColors
import com.example.ui.theme.ThemeMode
import com.google.firebase.auth.FirebaseUser

@Composable
fun UserAvatarButton(
    user: FirebaseUser?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalCashflowColors.current
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(semantic.subtleSurface)
            .border(1.dp, semantic.borderSubtle, CircleShape)
            .clickable(onClick = onClick)
            .testTag("account_avatar_button"),
        contentAlignment = Alignment.Center
    ) {
        val photoUrl = user?.photoUrl
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profil akun Google",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            )
        } else {
            val initial = (user?.displayName?.firstOrNull()
                ?: user?.email?.firstOrNull()
                ?: 'K').uppercaseChar()
            Text(
                text = initial.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AccountDialog(
    user: FirebaseUser?,
    totalSyncedCount: Int,
    themeMode: ThemeMode,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    val semantic = LocalCashflowColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = semantic.cardSurface,
        title = {
            Text(
                text = "Akun & Tampilan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatarButton(user = user, onClick = {})
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.displayName?.takeIf { it.isNotBlank() } ?: "Pengguna KasFlow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!user?.email.isNullOrBlank()) {
                            Text(
                                text = user?.email.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(semantic.mintBadge)
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(semantic.cardSurface.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDone,
                                contentDescription = null,
                                tint = semantic.mintText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Cloud Firestore Aktif",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = semantic.mintText
                            )
                            Text(
                                text = "$totalSyncedCount transaksi tersimpan real-time",
                                style = MaterialTheme.typography.bodySmall,
                                color = semantic.mintText.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Mode tampilan",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MinimalSegmentedTabs(
                        options = ThemeMode.entries,
                        selectedOption = themeMode,
                        labelFor = { it.label },
                        onSelect = onSelectThemeMode,
                        testTagPrefix = "theme_mode"
                    )
                }
            }
        },
        confirmButton = {
            MinimalButton(
                onClick = onSignOut,
                containerColor = semantic.expenseContainer,
                contentColor = semantic.expenseText,
                cornerRadius = 10.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier.testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Keluar Akun", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
