package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Book
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.RedError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditDialog(
    bookToEdit: Book?,
    onSaveBook: (Book) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = bookToEdit != null

    var titleAr by remember { mutableStateOf(bookToEdit?.titleAr ?: "") }
    var titleEn by remember { mutableStateOf(bookToEdit?.title ?: "") }
    var stage by remember { mutableStateOf(bookToEdit?.stage ?: "PRIMARY") }
    var gradeText by remember { mutableStateOf((bookToEdit?.grade ?: 4).toString()) }
    var academicYear by remember { mutableStateOf(bookToEdit?.academicYear ?: "2025/2026") }
    var subject by remember { mutableStateOf(bookToEdit?.subject ?: "الرياضيات") }
    var barcode by remember {
        mutableStateOf(
            bookToEdit?.barcode ?: "978-977-02-${(1000..9999).random()}-${(1..9).random()}"
        )
    }
    var minStockText by remember { mutableStateOf((bookToEdit?.minStockLevel ?: 100).toString()) }
    var currentStockText by remember { mutableStateOf((bookToEdit?.currentStock ?: 250).toString()) }
    var unitCostText by remember { mutableStateOf((bookToEdit?.unitCost ?: 40.0).toString()) }
    var description by remember { mutableStateOf(bookToEdit?.description ?: "") }

    var stageExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val stages = listOf(
        "PRIMARY" to "المرحلة الابتدائية (Primary)",
        "PREPARATORY" to "المرحلة الإعدادية (Preparatory)",
        "SECONDARY" to "المرحلة الثانوية (Secondary)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("book_edit_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = PrimaryTeal
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEdit) "تعديل بيانات الكتاب" else "إضافة كتاب جديد للفهرس",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = RedError,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Arabic Title
                OutlinedTextField(
                    value = titleAr,
                    onValueChange = { titleAr = it },
                    label = { Text("عنوان الكتاب (بالعربية) *") },
                    placeholder = { Text("مثال: الرياضيات - الصف الرابع الابتدائي") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_title_ar_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // English Title
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    label = { Text("العنوان (باللغة الإنجليزية)") },
                    placeholder = { Text("Primary Mathematics 4") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stage Dropdown
                ExposedDropdownMenuBox(
                    expanded = stageExpanded,
                    onExpandedChange = { stageExpanded = !stageExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = stages.firstOrNull { it.first == stage }?.second ?: stage,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المرحلة التعليمية") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stageExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = stageExpanded,
                        onDismissRequest = { stageExpanded = false }
                    ) {
                        stages.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    stage = code
                                    stageExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Grade
                    OutlinedTextField(
                        value = gradeText,
                        onValueChange = { gradeText = it },
                        label = { Text("الصف الدراسي (1-12)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Academic Year
                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("العام الدراسي") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subject
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("المادة الدراسية") },
                    placeholder = { Text("مثال: العلوم، الرياضيات، اللغة العربية...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Barcode / ISBN with regenerate button
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("الباركود / الرقم الدولي ISBN *") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                barcode = "978-977-02-${(1000..9999).random()}-${(1..9).random()}"
                            }
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = "توليد باركود تلقائي")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_barcode_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Min Stock Level
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("الحد الأدنى للأمان *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Current Stock Level
                    OutlinedTextField(
                        value = currentStockText,
                        onValueChange = { currentStockText = it },
                        label = { Text("الرصيد الفعلي *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Unit Cost
                OutlinedTextField(
                    value = unitCostText,
                    onValueChange = { unitCostText = it },
                    label = { Text("القيمة التقديرية للنسخة (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("ملاحظات / وصف الكتاب") },
                    placeholder = { Text("الفصل الدراسي الأول، الطبعة المعتمدة...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleAr.isBlank()) {
                        errorMessage = "يرجى إدخال عنوان الكتاب باللغة العربية"
                        return@Button
                    }
                    if (barcode.isBlank()) {
                        errorMessage = "يرجى إدخال أو توليد باركود للكتاب"
                        return@Button
                    }
                    val grade = gradeText.toIntOrNull() ?: 1
                    val minStock = minStockText.toIntOrNull() ?: 50
                    val currentStock = currentStockText.toIntOrNull() ?: 0
                    val unitCost = unitCostText.toDoubleOrNull() ?: 0.0

                    val newBook = Book(
                        id = bookToEdit?.id ?: 0L,
                        title = titleEn.ifBlank { titleAr },
                        titleAr = titleAr.trim(),
                        stage = stage,
                        grade = grade,
                        academicYear = academicYear.trim(),
                        subject = subject.trim(),
                        barcode = barcode.trim(),
                        minStockLevel = minStock,
                        currentStock = currentStock,
                        unitCost = unitCost,
                        description = description.trim(),
                        isSynced = false
                    )

                    onSaveBook(newBook)
                },
                modifier = Modifier.testTag("save_book_button")
            ) {
                Text(if (isEdit) "حفظ التعديلات" else "إضافة الكتاب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
