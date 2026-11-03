package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import com.example.ui.components.StockDistributionCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.data.model.Book
import com.example.data.model.InventoryTransaction
import com.example.ui.components.BreadcrumbsBar
import com.example.ui.components.LowStockTag
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatCard
import com.example.ui.components.SyncIndicatorBadge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.RedError
import com.example.ui.viewmodel.DashboardStats
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.SyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUser: AppUser,
    inventoryViewModel: InventoryViewModel,
    syncViewModel: SyncViewModel,
    onNavigateToCatalog: () -> Unit,
    onNavigateToInbound: () -> Unit,
    onNavigateToOutbound: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onSwitchUserClick: () -> Unit,
    onViewTransactionDetail: (Long) -> Unit,
    onOpenScanner: () -> Unit = {}
) {
    val allBooks by inventoryViewModel.allBooks.collectAsState()
    val stats by inventoryViewModel.stats.collectAsState()
    val lowStockBooks by inventoryViewModel.lowStockBooks.collectAsState()
    val recentTransactions by inventoryViewModel.recentTransactions.collectAsState()
    val academicYear by inventoryViewModel.selectedAcademicYear.collectAsState()
    val isSyncing by syncViewModel.isSyncing.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Breadcrumbs
        item {
            BreadcrumbsBar(items = listOf("نظام المستودعات", "لوحة المؤشرات الرئيسية (Dashboard)"))
        }

        // 2. Real-time Status Header (Academic Year, Role Badge, Sync Status)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GreenSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مستودع الكتب المدرسية المركزي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "العام الدراسي المعتمد: $academicYear",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SyncIndicatorBadge(
                            unsyncedCount = stats.unsyncedCount,
                            isSyncing = isSyncing,
                            onSyncClick = onNavigateToSync
                        )

                        RoleBadge(
                            user = currentUser,
                            onSwitchUserClick = onSwitchUserClick
                        )
                    }
                }
            }
        }

        // 3. Quick Action Stats Grid (Total Books, Physical Stock, Low Stock, Today's Transactions)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "فهرس العناوين",
                        value = "${stats.totalBooks}",
                        subtitle = "عنوان كتاب دراسي معتمد",
                        icon = Icons.Default.Book,
                        iconTint = PrimaryTeal,
                        onClick = onNavigateToCatalog,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "إجمالي الرصيد الفعلي",
                        value = "${stats.totalPhysicalStock}",
                        subtitle = "نسخة متاحة بالمستودع",
                        icon = Icons.Default.Inventory2,
                        iconTint = Color(0xFF0D9488),
                        onClick = onNavigateToCatalog,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "تنبيهات العجز الحرج",
                        value = "${stats.lowStockCount}",
                        subtitle = "كتب تحت حد الأمان الأدنى",
                        icon = Icons.Default.Warning,
                        iconTint = RedError,
                        badgeText = if (stats.lowStockCount > 0) "مطلوب توريد" else "مكتمل",
                        badgeColor = if (stats.lowStockCount > 0) RedError else GreenSuccess,
                        onClick = onNavigateToCatalog,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "حركات اليوم",
                        value = "${stats.todayTransactionsCount}",
                        subtitle = "أذون توريد وصرف اليوم",
                        icon = Icons.Default.ReceiptLong,
                        iconTint = Color(0xFF7C3AED),
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Quick Action Buttons Bar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "الإجراءات السريعة للمستودع:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToInbound,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_inbound_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إذن توريد (+)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToOutbound,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_outbound_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("صرف مدارس (-)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = onNavigateToAiAssistant,
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("quick_ai_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryTeal)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("المساعد الذكي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = onOpenScanner,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .testTag("quick_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "مسح باركود",
                                tint = PrimaryTeal
                            )
                        }
                    }
                }
            }
        }

        // 5. Interactive Stock Distribution Chart (Native Bar & Donut Charts)
        item {
            StockDistributionCard(books = allBooks)
        }

        // 6. Low Stock Alert Section (Real-time indicators)
        if (lowStockBooks.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(RedError.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = RedError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "كتب وصلت لمستوى العجز الحرج (${lowStockBooks.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RedError
                                )
                            }

                            TextButton(onClick = onNavigateToInbound) {
                                Text("إصدار أمر توريد", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        lowStockBooks.take(4).forEach { book ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = book.titleAr,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${book.stage} - الصف ${book.grade} | باركود: ${book.barcode}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${book.currentStock} / ${book.minStockLevel}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RedError
                                        )
                                        Text(
                                            text = "عجز ${book.minStockLevel - book.currentStock} نسخة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RedError
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Recent Transactions Feed
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "أحدث العمليات والأذون المخزنية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(onClick = onNavigateToTransactions) {
                            Text("عرض الكل", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (recentTransactions.isEmpty()) {
                        Text(
                            text = "لا توجد حركات مخزنية مسجلة بعد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        recentTransactions.take(6).forEach { tx ->
                            val isInbound = tx.type == "INBOUND"
                            val dateStr = SimpleDateFormat("dd/MM - HH:mm", Locale.US).format(Date(tx.timestamp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onViewTransactionDetail(tx.id) },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isInbound) GreenSuccess.copy(alpha = 0.15f)
                                                    else PrimaryTeal.copy(alpha = 0.15f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isInbound) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = if (isInbound) GreenSuccess else PrimaryTeal,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = tx.sourceOrDestination,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${tx.transactionNumber} • $dateStr",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isInbound) "+${tx.totalQuantity}" else "-${tx.totalQuantity}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isInbound) GreenSuccess else PrimaryTeal
                                        )
                                        Text(
                                            text = if (tx.isSynced) "مُزامن" else "أوفلاين",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (tx.isSynced) GreenSuccess else AmberWarning
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
