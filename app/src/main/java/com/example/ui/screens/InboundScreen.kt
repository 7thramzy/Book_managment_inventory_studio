package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.data.model.Book
import com.example.data.model.InventoryTransaction
import com.example.ui.components.BreadcrumbsBar
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.RedError
import com.example.ui.viewmodel.InventoryViewModel

data class InboundItemEntry(
    val book: Book,
    var quantity: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboundScreen(
    currentUser: AppUser,
    viewModel: InventoryViewModel,
    onTransactionCompleted: (InventoryTransaction) -> Unit
) {
    val allBooks by viewModel.allBooks.collectAsState()
    val academicYear by viewModel.selectedAcademicYear.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var supplierSource by remember { mutableStateOf("وزارة التربية والتعليم - مخازن الوزارة المركزية") }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val selectedItems = remember { mutableStateListOf<InboundItemEntry>() }

    var bookSelectorExpanded by remember { mutableStateOf(false) }
    var selectedBookForAddition by remember { mutableStateOf<Book?>(null) }
    var quantityToAddText by remember { mutableStateOf("100") }

    val presetSuppliers = listOf(
        "وزارة التربية والتعليم - مخازن الوزارة المركزية",
        "مطابع الأهرام للكتب المدرسية",
        "مطابع دار أخبار اليوم",
        "الهيئة العامة لشئون المطابع الأميرية",
        "توريد إضافي يدوي / تسوية مخزنية"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inbound_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BreadcrumbsBar(items = listOf("لوحة المؤشرات", "حركات التوريد (+)", "إصدار إذن توريد جديد"))
        }

        // Header Title Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GreenSuccess),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "إذن توريد واستلام كتب مدرسية (Inbound Entry)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenSuccess
                        )
                        Text(
                            text = "تزيد هذه العملية أرصدة الكتب تلقائياً وتُسجل أوفلاين مع إمكانية المزامنة مع Google Sheets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Supplier & Basic Info Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "بيانات الجهة الموردة والإذن:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = supplierSource,
                        onValueChange = { supplierSource = it },
                        label = { Text("الجهة المورّدة / المورد *") },
                        placeholder = { Text("وزارة التربية والتعليم أو اسم المطبعة") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inbound_supplier_input"),
                        leadingIcon = {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = GreenSuccess)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset supplier quick tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "اقتراحات:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        presetSuppliers.take(2).forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { supplierSource = preset }
                            ) {
                                Text(
                                    text = preset.substringBefore(" -"),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = academicYear,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("العام الدراسي المستهدف") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = currentUser.fullNameAr,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("أمين المستودع المستلم") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إذن التوريد / رقم البوليصة") },
                        placeholder = { Text("رقم إذن استلام مطبعة أو رقم فاتورة...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Add Books Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إضافة الكتب الموردة للشحنة:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dropdown book selector
                    ExposedDropdownMenuBox(
                        expanded = bookSelectorExpanded,
                        onExpandedChange = { bookSelectorExpanded = !bookSelectorExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedBookForAddition?.let { "${it.titleAr} (الرصيد: ${it.currentStock})" } ?: "اختر كتاباً من الفهرس...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("اختر الكتاب المراد توريده") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookSelectorExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("select_book_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = bookSelectorExpanded,
                            onDismissRequest = { bookSelectorExpanded = false }
                        ) {
                            allBooks.forEach { book ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(book.titleAr, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${book.stage} - الصف ${book.grade} | الرصيد الحالي: ${book.currentStock}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedBookForAddition = book
                                        bookSelectorExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = quantityToAddText,
                            onValueChange = { quantityToAddText = it },
                            label = { Text("الكمية المورّدة") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("inbound_qty_input"),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val book = selectedBookForAddition
                                val qty = quantityToAddText.toIntOrNull() ?: 0
                                if (book != null && qty > 0) {
                                    val existing = selectedItems.find { it.book.id == book.id }
                                    if (existing != null) {
                                        existing.quantity += qty
                                    } else {
                                        selectedItems.add(InboundItemEntry(book, qty))
                                    }
                                    selectedBookForAddition = null
                                    quantityToAddText = "100"
                                    validationError = null
                                }
                            },
                            enabled = selectedBookForAddition != null && (quantityToAddText.toIntOrNull() ?: 0) > 0,
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("add_item_to_inbound_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إدراج بالقائمة")
                        }
                    }
                }
            }
        }

        // List of Selected Books in this Inbound
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
                            text = "قائمة الكتب في إذن التوريد (${selectedItems.size} كتب):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "الإجمالي: ${selectedItems.sumOf { it.quantity }} نسخة",
                            fontWeight = FontWeight.Bold,
                            color = GreenSuccess
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedItems.isEmpty()) {
                        Text(
                            text = "لم تتم إضافة كتب إلى إذن التوريد حتى الآن. اختر كتاباً وحدد الكمية بالأعلى.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        selectedItems.forEachIndexed { index, item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${index + 1}. ${item.book.titleAr}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "الرصيد الحالي: ${item.book.currentStock} -> بعد التوريد: ${item.book.currentStock + item.quantity}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GreenSuccess
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = GreenSuccess.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "+${item.quantity} نسخة",
                                                fontWeight = FontWeight.Bold,
                                                color = GreenSuccess,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 12.sp
                                            )
                                        }

                                        IconButton(onClick = { selectedItems.remove(item) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedError)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = validationError!!,
                            color = RedError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = RedError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Inbound Button
                    Button(
                        onClick = {
                            if (supplierSource.isBlank()) {
                                validationError = "يرجى تحديد الجهة المورّدة"
                                return@Button
                            }
                            if (selectedItems.isEmpty()) {
                                validationError = "يرجى إضافة كتاب واحد على الأقل لإذن التوريد"
                                return@Button
                            }

                            validationError = null
                            val itemsToSubmit = selectedItems.map { it.book to it.quantity }
                            viewModel.submitInboundTransaction(
                                source = supplierSource.trim(),
                                officerName = currentUser.fullNameAr,
                                notes = notes.trim(),
                                items = itemsToSubmit,
                                onSuccess = { tx ->
                                    onTransactionCompleted(tx)
                                }
                            )
                        },
                        enabled = !isProcessing && selectedItems.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_inbound_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اعتماد إذن التوريد وزيادة المخزون", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
