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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.School
import com.example.ui.components.BreadcrumbsBar
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.RedError
import com.example.ui.theme.RedErrorContainer
import com.example.ui.viewmodel.InventoryViewModel

data class OutboundItemEntry(
    val book: Book,
    var quantity: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutboundScreen(
    currentUser: AppUser,
    viewModel: InventoryViewModel,
    onTransactionCompleted: (InventoryTransaction) -> Unit
) {
    val allBooks by viewModel.allBooks.collectAsState()
    val allSchools by viewModel.allSchools.collectAsState()
    val academicYear by viewModel.selectedAcademicYear.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedSchool by remember { mutableStateOf<School?>(allSchools.firstOrNull()) }
    var schoolDropdownExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val selectedItems = remember { mutableStateListOf<OutboundItemEntry>() }

    var bookSelectorExpanded by remember { mutableStateOf(false) }
    var selectedBookForAddition by remember { mutableStateOf<Book?>(null) }
    var quantityToAddText by remember { mutableStateOf("50") }

    // Update default selected school once schools load
    if (selectedSchool == null && allSchools.isNotEmpty()) {
        selectedSchool = allSchools.first()
    }

    // Strict Inventory Validation: Check for any item whose requested quantity exceeds current available stock!
    val hasNegativeStockRisk = selectedItems.any { it.quantity > it.book.currentStock }
    val invalidItem = selectedItems.firstOrNull { it.quantity > it.book.currentStock }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("outbound_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BreadcrumbsBar(items = listOf("لوحة المؤشرات", "حركات الصرف (-)", "صرف وتوزيع مدارس"))
        }

        // Header Title Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.1f))
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
                            .background(PrimaryTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "إذن صرف وتوزيع للمدارس (School Distribution)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                        Text(
                            text = "نظام تحقق صارم يمنع الصرف بالسالب ويخصم الرصيد فوراً مع تسجيل إيصال رسمي.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Destination School Selection Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "بيانات المدرسة المستلمة والإذن:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // School Dropdown
                    ExposedDropdownMenuBox(
                        expanded = schoolDropdownExpanded,
                        onExpandedChange = { schoolDropdownExpanded = !schoolDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedSchool?.nameAr ?: "اختر مدرسة مستلمة...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المدرسة المستلمة *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schoolDropdownExpanded) },
                            leadingIcon = {
                                Icon(Icons.Default.School, contentDescription = null, tint = PrimaryTeal)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("select_school_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = schoolDropdownExpanded,
                            onDismissRequest = { schoolDropdownExpanded = false }
                        ) {
                            allSchools.forEach { school ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(school.nameAr, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${school.district} • مندوب: ${school.contactPerson} (${school.phone})",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedSchool = school
                                        schoolDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedSchool != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "المندوب المعتمد: ${selectedSchool!!.contactPerson} | هاتف: ${selectedSchool!!.phone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
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
                            label = { Text("العام الدراسي") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = currentUser.fullNameAr,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("أمين المستودع المصرح") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إذن الصرف / رقم الاستلام") },
                        placeholder = { Text("حصة الفصل الدراسي الأول، مندوب المدرسة...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Add Books to Outbound Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اختيار الكتب وتحديد الكميات المطلوبة:",
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
                            value = selectedBookForAddition?.let { "${it.titleAr} (الرصيد المتاح: ${it.currentStock})" } ?: "اختر كتاباً للصرف...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الكتاب المراد صرفه") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookSelectorExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("select_outbound_book_dropdown")
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
                                                "${book.stage} - الصف ${book.grade} | المتاح بالمخزن: ${book.currentStock} نسخة",
                                                fontSize = 11.sp,
                                                color = if (book.currentStock == 0) RedError else MaterialTheme.colorScheme.outline
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
                            label = { Text("الكمية المطلوبة") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("outbound_qty_input"),
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
                                        selectedItems.add(OutboundItemEntry(book, qty))
                                    }
                                    selectedBookForAddition = null
                                    quantityToAddText = "50"
                                    validationError = null
                                }
                            },
                            enabled = selectedBookForAddition != null && (quantityToAddText.toIntOrNull() ?: 0) > 0,
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("add_item_to_outbound_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
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

        // Strict Inventory Validation Warning Banner (if requested > stock)
        if (hasNegativeStockRisk && invalidItem != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RedErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("strict_validation_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = RedError,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "تحذير رقابي صارم: عجز في الرصيد المخزني!",
                                fontWeight = FontWeight.Bold,
                                color = RedError,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "الكتاب '${invalidItem.book.titleAr}' رصيده المتاح (${invalidItem.book.currentStock} نسخة) أقل من الكمية المطلوبة للصرف (${invalidItem.quantity} نسخة). يُمنع نظاماً صرف كميات سالبة.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Selected Items List
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
                            text = "قائمة الكتب في إذن الصرف (${selectedItems.size} كتب):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "الإجمالي: ${selectedItems.sumOf { it.quantity }} نسخة",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedItems.isEmpty()) {
                        Text(
                            text = "لم تتم إضافة أي كتب إلى إذن الصرف بعد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        selectedItems.forEachIndexed { index, item ->
                            val isItemDeficient = item.quantity > item.book.currentStock

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isItemDeficient) RedErrorContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                                            text = "الرصيد المتاح: ${item.book.currentStock} نسخة | المتبقي بعد الصرف: ${item.book.currentStock - item.quantity} نسخة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isItemDeficient) RedError else PrimaryTeal,
                                            fontWeight = if (isItemDeficient) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isItemDeficient) RedErrorContainer else PrimaryTeal.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "-${item.quantity} نسخة",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isItemDeficient) RedError else PrimaryTeal,
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

                    // Submit Outbound Button (Disabled if negative stock risk or empty)
                    Button(
                        onClick = {
                            if (selectedSchool == null) {
                                validationError = "يرجى اختيار مدرسة مستلمة"
                                return@Button
                            }
                            if (selectedItems.isEmpty()) {
                                validationError = "يرجى إضافة كتب لإذن الصرف"
                                return@Button
                            }
                            if (hasNegativeStockRisk) {
                                validationError = "لا يمكن اعتماد الصرف: توجد كميات تتجاوز الرصيد المتاح بالمخزن"
                                return@Button
                            }

                            validationError = null
                            val itemsToSubmit = selectedItems.map { it.book to it.quantity }
                            viewModel.submitOutboundTransaction(
                                school = selectedSchool!!,
                                officerName = currentUser.fullNameAr,
                                notes = notes.trim(),
                                items = itemsToSubmit,
                                onSuccess = { tx ->
                                    onTransactionCompleted(tx)
                                }
                            )
                        },
                        enabled = !isProcessing && selectedItems.isNotEmpty() && !hasNegativeStockRisk,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_outbound_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (hasNegativeStockRisk) "مرفوض: عجز في الرصيد المتاح" else "اعتماد إذن الصرف وخصم المخزون",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
