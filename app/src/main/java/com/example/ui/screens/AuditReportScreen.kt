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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.ui.components.BreadcrumbsBar
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.RedError
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.ReportExportService
import kotlinx.coroutines.launch

@Composable
fun AuditReportScreen(
    currentUser: AppUser,
    viewModel: InventoryViewModel,
    onNavigateToAiAudit: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val stats by viewModel.stats.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()
    val lowStockBooks by viewModel.lowStockBooks.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val academicYear by viewModel.selectedAcademicYear.collectAsState()

    var isExporting by remember { mutableStateOf(false) }
    var exportStatusText by remember { mutableStateOf<String?>(null) }

    val inboundCount = allTransactions.count { it.type == "INBOUND" }
    val outboundCount = allTransactions.count { it.type == "OUTBOUND" }
    val totalInboundQty = allTransactions.filter { it.type == "INBOUND" }.sumOf { it.totalQuantity }
    val totalOutboundQty = allTransactions.filter { it.type == "OUTBOUND" }.sumOf { it.totalQuantity }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("audit_report_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BreadcrumbsBar(items = listOf("لوحة المؤشرات", "المطابقة والتدقيق الرقابي (Auditor Reconciliation)"))
        }

        // Header Title Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D9488)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "تقرير التدقيق والمطابقة الدفترية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "صلاحيات المدقق: مراجعة حركة الأرصدة، منع التجاوزات، واكتشاف فجوات التوريد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Reconciliation Metrics Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مؤشرات المطابقة والتدفق المخزني:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("إجمالي الوارد (التوريدات):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("$totalInboundQty نسخة ($inboundCount أذون)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("إجمالي المنصرف (المدارس):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("$totalOutboundQty نسخة ($outboundCount أذون)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الرصيد الفعلي الحالي:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("${stats.totalPhysicalStock} نسخة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("نسبة الجاهزية والاستقرار:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            val readiness = if (allBooks.isNotEmpty()) ((allBooks.size - lowStockBooks.size) * 100 / allBooks.size) else 100
                            Text("$readiness%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (readiness > 80) GreenSuccess else RedError)
                        }
                    }
                }
            }
        }

        // PDF & CSV Summary Reports Generation Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.testTag("export_reports_card")
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryTeal.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تصدير تقارير التدقيق والمطابقة (PDF & CSV)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "إنشاء وثائق PDF رسمية وقوائم CSV متوافقة مع Excel للمراجعة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Current Inventory Reports (PDF & CSV)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "1. تقرير الجرد الفعلي للمخزون (Inventory Summary)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "يشمل تفاصيل كافة الكتب، الأرصدة، الحدود الدنيا، ونسب العجز.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isExporting = true
                                            exportStatusText = "جارٍ إنشاء تقرير المخزون بصيغة PDF..."
                                            val result = ReportExportService.generateInventoryPdf(context, allBooks, stats, academicYear)
                                            isExporting = false
                                            result.onSuccess { uri ->
                                                exportStatusText = "تم إنشاء ملف PDF بنجاح!"
                                                ReportExportService.shareReportFile(context, uri, "application/pdf", "تقرير جرد المخزون - KitabTrack")
                                            }.onFailure { err ->
                                                exportStatusText = "فشل التصدير: ${err.localizedMessage}"
                                            }
                                        }
                                    },
                                    enabled = !isExporting && allBooks.isNotEmpty(),
                                    modifier = Modifier.weight(1f).testTag("export_inventory_pdf_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تصدير PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isExporting = true
                                            exportStatusText = "جارٍ تجهيز ملف CSV..."
                                            val result = ReportExportService.generateInventoryCsv(context, allBooks)
                                            isExporting = false
                                            result.onSuccess { uri ->
                                                exportStatusText = "تم إنشاء ملف CSV بنجاح!"
                                                ReportExportService.shareReportFile(context, uri, "text/csv", "قائمة المخزون Excel/CSV - KitabTrack")
                                            }.onFailure { err ->
                                                exportStatusText = "فشل التصدير: ${err.localizedMessage}"
                                            }
                                        }
                                    },
                                    enabled = !isExporting && allBooks.isNotEmpty(),
                                    modifier = Modifier.weight(1f).testTag("export_inventory_csv_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryTeal)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تصدير CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Transactions Audit Log Reports (PDF & CSV)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "2. سجل التدقيق للحركات والأذون (Transactions Log)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "يشمل كافة أذون التوريد والصرف مع أرقام القيود ومندوبي الاستلام.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isExporting = true
                                            exportStatusText = "جارٍ إنشاء سجل الحركات بصيغة PDF..."
                                            val result = ReportExportService.generateTransactionsPdf(context, allTransactions, academicYear)
                                            isExporting = false
                                            result.onSuccess { uri ->
                                                exportStatusText = "تم إنشاء ملف PDF بنجاح!"
                                                ReportExportService.shareReportFile(context, uri, "application/pdf", "سجل الحركات والأذون - KitabTrack")
                                            }.onFailure { err ->
                                                exportStatusText = "فشل التصدير: ${err.localizedMessage}"
                                            }
                                        }
                                    },
                                    enabled = !isExporting && allTransactions.isNotEmpty(),
                                    modifier = Modifier.weight(1f).testTag("export_tx_pdf_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("سجل أذون PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isExporting = true
                                            exportStatusText = "جارٍ إنشاء ملف CSV للحركات..."
                                            val result = ReportExportService.generateTransactionsCsv(context, allTransactions)
                                            isExporting = false
                                            result.onSuccess { uri ->
                                                exportStatusText = "تم إنشاء ملف CSV للحركات بنجاح!"
                                                ReportExportService.shareReportFile(context, uri, "text/csv", "سجل الحركات Excel/CSV - KitabTrack")
                                            }.onFailure { err ->
                                                exportStatusText = "فشل التصدير: ${err.localizedMessage}"
                                            }
                                        }
                                    },
                                    enabled = !isExporting && allTransactions.isNotEmpty(),
                                    modifier = Modifier.weight(1f).testTag("export_tx_csv_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0D9488))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("سجل CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (exportStatusText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryTeal.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(exportStatusText!!, fontSize = 11.sp, color = PrimaryTeal, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // AI Smart Audit Assistant Prompt Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تدقيق ذكي فوري (Gemini 3.1 Flash Lite)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تحليل فوري للفجوات وحساب الكميات المطلوبة للتوريد وفق الاستهلاك الفعلي.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onNavigateToAiAudit,
                        modifier = Modifier.padding(start = 12.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("بدء التدقيق")
                    }
                }
            }
        }

        // Critical Low Stock Table for Auditor
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
                            text = "تقرير الفجوات والعجز (${lowStockBooks.size} كتب):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (lowStockBooks.isEmpty()) GreenSuccess else RedError
                        )

                        if (lowStockBooks.isEmpty()) {
                            Text(
                                text = "مطابق للمعايير 100%",
                                color = GreenSuccess,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (lowStockBooks.isEmpty()) {
                        Text(
                            text = "لا توجد كتب تحت حد الأمان الأدنى. جميع الأرصدة كافية ومطابقة لخطط التوزيع.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        lowStockBooks.forEach { book ->
                            val deficit = book.minStockLevel - book.currentStock
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
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(book.titleAr, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("${book.stage} - الصف ${book.grade}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "عجز: $deficit نسخة",
                                            fontWeight = FontWeight.Bold,
                                            color = RedError,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "الرصيد: ${book.currentStock} / ${book.minStockLevel}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
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
