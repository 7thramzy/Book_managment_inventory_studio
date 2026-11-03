package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryTransaction
import com.example.ui.components.CameraBarcodeScannerDialog
import com.example.ui.components.RoleBadge
import com.example.ui.components.SwitchUserDialog
import com.example.ui.components.SyncIndicatorBadge
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AuditReportScreen
import com.example.ui.screens.BooksCatalogScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InboundScreen
import com.example.ui.screens.OutboundScreen
import com.example.ui.screens.SyncScreen
import com.example.ui.screens.TransactionsHistoryScreen
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.AiAssistantViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.SyncViewModel
import kotlinx.coroutines.launch

enum class AppScreen(val titleAr: String, val icon: ImageVector) {
    DASHBOARD("لوحة المؤشرات", Icons.Default.Dashboard),
    CATALOG("فهرس ومخزون الكتب", Icons.Default.MenuBook),
    INBOUND("إذن توريد كتب (+)", Icons.Default.ArrowDownward),
    OUTBOUND("صرف وتوزيع مدارس (-)", Icons.Default.ArrowUpward),
    TRANSACTIONS("سجل الحركات والأذون", Icons.Default.ReceiptLong),
    AUDIT("التدقيق والمطابقة", Icons.Default.VerifiedUser),
    SYNC("المزامنة و Google Sheets", Icons.Default.CloudSync),
    AI_ASSISTANT("المساعد الذكي (Gemini)", Icons.Default.AutoAwesome)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    authViewModel: AuthViewModel,
    inventoryViewModel: InventoryViewModel,
    syncViewModel: SyncViewModel,
    aiViewModel: AiAssistantViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val unsyncedCount by inventoryViewModel.unsyncedCount.collectAsState()
    val isSyncing by syncViewModel.isSyncing.collectAsState()
    val activeTxDetail by inventoryViewModel.activeTransactionDetail.collectAsState()
    val allBooks by inventoryViewModel.allBooks.collectAsState()
    val allTransactions by inventoryViewModel.allTransactions.collectAsState()
    val successMsg by inventoryViewModel.successMessage.collectAsState()
    val errorMsg by inventoryViewModel.errorMessage.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var showUserSwitchDialog by remember { mutableStateOf(false) }
    var showGlobalScanner by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept back button when drawer is open or on subscreen
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    LaunchedEffect(successMsg) {
        if (successMsg != null) {
            snackbarHostState.showSnackbar(successMsg!!)
            inventoryViewModel.dismissMessages()
        }
    }

    LaunchedEffect(errorMsg) {
        if (errorMsg != null) {
            snackbarHostState.showSnackbar(errorMsg!!)
            inventoryViewModel.dismissMessages()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight(),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryTeal)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "KitabTrack",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "نظام إدارة مخازن الكتب المدرسية",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showUserSwitchDialog = true
                                    scope.launch { drawerState.close() }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentUser.fullNameAr,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${currentUser.role.labelAr} (انقر للتبديل)",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Drawer Navigation Items
                AppScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = null,
                                tint = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = screen.titleAr,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        badge = {
                            if (screen == AppScreen.SYNC && unsyncedCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFD97706)
                                ) {
                                    Text(
                                        text = "$unsyncedCount",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        selected = isSelected,
                        onClick = {
                            currentScreen = screen
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_${screen.name}"),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PrimaryTeal.copy(alpha = 0.12f)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()

                // Footer
                Text(
                    text = "KitabTrack v1.0 • Offline-First Room DB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentScreen.titleAr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "KitabTrack Inventory",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryTeal
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "القائمة الجانبية")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showGlobalScanner = true },
                            modifier = Modifier.testTag("global_top_bar_scanner_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "الماسح الضوئي العام (QR/Barcode)",
                                tint = PrimaryTeal
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        SyncIndicatorBadge(
                            unsyncedCount = unsyncedCount,
                            isSyncing = isSyncing,
                            onSyncClick = { currentScreen = AppScreen.SYNC }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        RoleBadge(
                            user = currentUser,
                            onSwitchUserClick = { showUserSwitchDialog = true }
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    val bottomScreens = listOf(
                        AppScreen.DASHBOARD,
                        AppScreen.CATALOG,
                        AppScreen.INBOUND,
                        AppScreen.OUTBOUND,
                        AppScreen.AI_ASSISTANT
                    )

                    bottomScreens.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.titleAr) },
                            label = { Text(screen.titleAr.substringBefore(" (").take(10), fontSize = 10.sp) },
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryTeal,
                                selectedTextColor = PrimaryTeal,
                                indicatorColor = PrimaryTeal.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        currentUser = currentUser,
                        inventoryViewModel = inventoryViewModel,
                        syncViewModel = syncViewModel,
                        onNavigateToCatalog = { currentScreen = AppScreen.CATALOG },
                        onNavigateToInbound = { currentScreen = AppScreen.INBOUND },
                        onNavigateToOutbound = { currentScreen = AppScreen.OUTBOUND },
                        onNavigateToTransactions = { currentScreen = AppScreen.TRANSACTIONS },
                        onNavigateToSync = { currentScreen = AppScreen.SYNC },
                        onNavigateToAiAssistant = { currentScreen = AppScreen.AI_ASSISTANT },
                        onSwitchUserClick = { showUserSwitchDialog = true },
                        onViewTransactionDetail = { txId -> inventoryViewModel.loadTransactionDetail(txId) },
                        onOpenScanner = { showGlobalScanner = true }
                    )

                    AppScreen.CATALOG -> BooksCatalogScreen(
                        currentUser = currentUser,
                        viewModel = inventoryViewModel
                    )

                    AppScreen.INBOUND -> InboundScreen(
                        currentUser = currentUser,
                        viewModel = inventoryViewModel,
                        onTransactionCompleted = { tx ->
                            inventoryViewModel.loadTransactionDetail(tx.id)
                        }
                    )

                    AppScreen.OUTBOUND -> OutboundScreen(
                        currentUser = currentUser,
                        viewModel = inventoryViewModel,
                        onTransactionCompleted = { tx ->
                            inventoryViewModel.loadTransactionDetail(tx.id)
                        }
                    )

                    AppScreen.TRANSACTIONS -> TransactionsHistoryScreen(
                        viewModel = inventoryViewModel,
                        onViewTransactionDetail = { txId -> inventoryViewModel.loadTransactionDetail(txId) }
                    )

                    AppScreen.AUDIT -> AuditReportScreen(
                        currentUser = currentUser,
                        viewModel = inventoryViewModel,
                        onNavigateToAiAudit = {
                            currentScreen = AppScreen.AI_ASSISTANT
                            aiViewModel.sendMessage("قم بإجراء تدقيق رقابي شامل ومطابقة المخزون الفعلي مع الحدود الدنيا للأمان واقتراح خطة توريد.")
                        }
                    )

                    AppScreen.SYNC -> SyncScreen(
                        currentUser = currentUser,
                        syncViewModel = syncViewModel,
                        inventoryViewModel = inventoryViewModel
                    )

                    AppScreen.AI_ASSISTANT -> AiAssistantScreen(
                        viewModel = aiViewModel
                    )
                }
            }
        }
    }

    // Role switcher dialog
    if (showUserSwitchDialog) {
        SwitchUserDialog(
            currentUser = currentUser,
            availableUsers = authViewModel.availableUsers,
            onSelectUser = { user, pin ->
                authViewModel.switchUser(user, pin)
            },
            onQuickSwitch = { role ->
                authViewModel.quickSwitchRole(role)
            },
            onDismiss = { showUserSwitchDialog = false }
        )
    }

    // Transaction Receipt Modal
    if (activeTxDetail != null) {
        TransactionReceiptDialog(
            transactionWithItems = activeTxDetail!!,
            onDismiss = { inventoryViewModel.clearActiveTransactionDetail() }
        )
    }

    // Global QR / Barcode Scanner Modal
    if (showGlobalScanner) {
        CameraBarcodeScannerDialog(
            booksCatalog = allBooks,
            onBarcodeScanned = { scannedCode ->
                showGlobalScanner = false
                val matchedTx = allTransactions.find {
                    it.transactionNumber.equals(scannedCode, ignoreCase = true) ||
                    it.transactionNumber.replace("-", "").equals(scannedCode.replace("-", ""), ignoreCase = true)
                }
                if (matchedTx != null) {
                    inventoryViewModel.loadTransactionDetail(matchedTx.id)
                } else {
                    currentScreen = AppScreen.CATALOG
                    inventoryViewModel.setSearchQuery(scannedCode)
                }
            },
            onDismiss = { showGlobalScanner = false }
        )
    }
}

