package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.auth.AuthScreen
import com.example.auth.signOut
import com.example.data.TransactionRepository
import com.example.model.Transaction
import com.example.model.UiState
import com.example.ui.components.AccountDialog
import com.example.ui.components.BrandRpBadge
import com.example.ui.components.CloudSyncPill
import com.example.ui.components.CloudSyncStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MinimalButton
import com.example.ui.components.MinimalCard
import com.example.ui.components.ThemeToggleButton
import com.example.ui.components.TransactionEditorSheet
import com.example.ui.components.UserAvatarButton
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.LocalCashflowColors
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.ActionFeedback
import com.example.ui.viewmodel.CashflowDashboardState
import com.example.ui.viewmodel.CashflowViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private enum class AppTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Beranda", Icons.Rounded.Home, Icons.Outlined.Home, "tab_dashboard"),
    ANALYTICS("Laporan", Icons.Rounded.BarChart, Icons.Outlined.BarChart, "tab_analytics"),
    TRANSACTIONS("Transaksi", Icons.Rounded.ReceiptLong, Icons.Outlined.ReceiptLong, "tab_transactions")
}

@Composable
fun CashflowApp(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onToggleTheme: () -> Unit = {},
    onSelectThemeMode: (ThemeMode) -> Unit = {}
) {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onToggleTheme = onToggleTheme,
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        key(user.uid) {
            AuthenticatedCashflowShell(
                user = user,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onSelectThemeMode = onSelectThemeMode,
                onSignedOut = { currentUser = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedCashflowShell(
    user: FirebaseUser,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val semantic = LocalCashflowColors.current

    val viewModel: CashflowViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CashflowViewModel(
                    repository = TransactionRepository(db),
                    currentUserId = user.uid
                )
            }
        }
    )

    val dashboardUiState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val feedback by viewModel.actionFeedback.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var activeTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var showEditor by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<Transaction?>(null) }
    var showAccountDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = activeTab != AppTab.DASHBOARD && !showEditor && !showAccountDialog) {
        activeTab = AppTab.DASHBOARD
    }

    LaunchedEffect(feedback) {
        when (val fb = feedback) {
            is ActionFeedback.Success -> {
                snackbarHostState.showSnackbar(fb.message)
                viewModel.clearFeedback()
            }
            is ActionFeedback.Error -> {
                snackbarHostState.showSnackbar(fb.message)
                viewModel.clearFeedback()
            }
            else -> Unit
        }
    }

    val syncStatus = when {
        feedback is ActionFeedback.Saving -> CloudSyncStatus.SYNCING
        dashboardUiState is UiState.Loading -> CloudSyncStatus.SYNCING
        dashboardUiState is UiState.Error -> CloudSyncStatus.ERROR
        else -> CloudSyncStatus.SYNCED
    }

    val successState = (dashboardUiState as? UiState.Success<CashflowDashboardState>)?.data

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(semantic.canvasBg)
    ) {
        val useSidebar = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useSidebar) {
                MinimalSidebar(
                    user = user,
                    activeTab = activeTab,
                    onSelectTab = { activeTab = it },
                    onToggleTheme = onToggleTheme,
                    onOpenAccount = { showAccountDialog = true }
                )
                VerticalDivider(thickness = 1.dp, color = semantic.borderSubtle)
            }

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    Column {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = semantic.barSurface
                            ),
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (!useSidebar) {
                                        BrandRpBadge(size = 32.dp)
                                    }
                                    Column {
                                        Text(
                                            text = if (useSidebar) {
                                                activeTab.label
                                            } else {
                                                stringResource(id = R.string.app_name)
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (!useSidebar) {
                                            Text(
                                                text = "Catatan kas cloud",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    CloudSyncPill(status = syncStatus)
                                }
                            },
                            actions = {
                                if (!useSidebar) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(end = 10.dp)
                                    ) {
                                        ThemeToggleButton(onToggle = onToggleTheme)
                                        UserAvatarButton(
                                            user = user,
                                            onClick = { showAccountDialog = true }
                                        )
                                    }
                                }
                            }
                        )
                        HorizontalDivider(thickness = 1.dp, color = semantic.borderSubtle)
                    }
                },
                bottomBar = {
                    if (!useSidebar) {
                        Column {
                            HorizontalDivider(thickness = 1.dp, color = semantic.borderSubtle)
                            NavigationBar(
                                containerColor = semantic.barSurface,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                AppTab.entries.forEach { tab ->
                                    val selected = activeTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { activeTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = semantic.primaryAction,
                                            selectedTextColor = semantic.primaryAction,
                                            indicatorColor = semantic.subtleSurface,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (successState != null) {
                        MinimalButton(
                            onClick = {
                                editingRecord = null
                                showEditor = true
                            },
                            containerColor = semantic.primaryAction,
                            contentColor = semantic.onPrimaryAction,
                            cornerRadius = 14.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("add_transaction_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Tambah transaksi",
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Catat",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (val ui = dashboardUiState) {
                        is UiState.Loading -> {
                            MinimalCard(
                                containerColor = semantic.cardSurface,
                                borderColor = semantic.borderSubtle,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = semantic.primaryAction,
                                        strokeWidth = 2.5.dp
                                    )
                                    Text(
                                        text = "Menyinkronkan data cloud...",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        is UiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyStateCard(
                                    icon = Icons.Rounded.CloudOff,
                                    title = "Sinkronisasi Cloud Terganggu",
                                    subtitle = ui.message
                                )
                            }
                        }

                        is UiState.Success -> {
                            val state = ui.data
                            when (activeTab) {
                                AppTab.DASHBOARD -> {
                                    DashboardScreen(
                                        state = state,
                                        onPreviousMonth = viewModel::previousMonth,
                                        onNextMonth = viewModel::nextMonth,
                                        onSelectMonth = viewModel::selectYearMonth,
                                        onAddTransaction = {
                                            editingRecord = null
                                            showEditor = true
                                        },
                                        onOpenAnalytics = { activeTab = AppTab.ANALYTICS },
                                        onOpenAllTransactions = { activeTab = AppTab.TRANSACTIONS },
                                        onEditTransaction = { tx ->
                                            editingRecord = tx
                                            showEditor = true
                                        }
                                    )
                                }

                                AppTab.ANALYTICS -> {
                                    AnalyticsScreen(
                                        state = state,
                                        onPreviousMonth = viewModel::previousMonth,
                                        onNextMonth = viewModel::nextMonth,
                                        onSelectMonth = viewModel::selectYearMonth
                                    )
                                }

                                AppTab.TRANSACTIONS -> {
                                    TransactionsScreen(
                                        state = state,
                                        onPreviousMonth = viewModel::previousMonth,
                                        onNextMonth = viewModel::nextMonth,
                                        onSearchChange = viewModel::setSearchQuery,
                                        onTypeFilterChange = viewModel::setTypeFilter,
                                        onCategoryFilterChange = viewModel::setCategoryFilter,
                                        onEditTransaction = { tx ->
                                            editingRecord = tx
                                            showEditor = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor && successState != null) {
        TransactionEditorSheet(
            editingRecord = editingRecord,
            selectedYearMonth = successState.selectedYearMonth,
            isSaving = feedback is ActionFeedback.Saving,
            onDismiss = {
                showEditor = false
                editingRecord = null
            },
            onSave = { id, title, type, category, amount, note, dateMillis ->
                viewModel.saveTransaction(
                    editingId = id,
                    title = title,
                    type = type,
                    category = category,
                    amount = amount,
                    note = note,
                    date = dateMillis,
                    onComplete = {
                        showEditor = false
                        editingRecord = null
                    }
                )
            },
            onDelete = { txId ->
                viewModel.deleteTransaction(txId) {
                    showEditor = false
                    editingRecord = null
                }
            }
        )
    }

    if (showAccountDialog) {
        AccountDialog(
            user = user,
            totalSyncedCount = successState?.totalSyncedCount ?: 0,
            themeMode = themeMode,
            onSelectThemeMode = onSelectThemeMode,
            onDismiss = { showAccountDialog = false },
            onSignOut = {
                showAccountDialog = false
                signOut(
                    credentialManager = credentialManager,
                    scope = scope,
                    onSignOutComplete = onSignedOut
                )
            }
        )
    }
}

@Composable
private fun MinimalSidebar(
    user: FirebaseUser,
    activeTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    onToggleTheme: () -> Unit,
    onOpenAccount: () -> Unit
) {
    val semantic = LocalCashflowColors.current
    Column(
        modifier = Modifier
            .width(216.dp)
            .fillMaxHeight()
            .background(semantic.barSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 14.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BrandRpBadge(size = 36.dp)
                Column {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Buku kas pribadi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "MENU",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )

                AppTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) semantic.primaryAction else Color.Transparent)
                            .clickable { onSelectTab(tab) }
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                            .testTag(tab.testTag),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = if (selected) semantic.onPrimaryAction else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) semantic.onPrimaryAction else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, semantic.borderSubtle, RoundedCornerShape(12.dp))
                    .background(semantic.subtleSurface)
                    .clickable(onClick = onToggleTheme)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (semantic.isDark) "Mode Gelap" else "Mode Terang",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ThemeToggleButton(onToggle = onToggleTheme)
            }
            HorizontalDivider(thickness = 1.dp, color = semantic.borderSubtle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenAccount)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UserAvatarButton(user = user, onClick = onOpenAccount)
                Text(
                    text = user.displayName?.takeIf { it.isNotBlank() } ?: "Akun Google",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
