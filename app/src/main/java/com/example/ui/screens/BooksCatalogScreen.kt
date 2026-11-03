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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.data.model.Book
import com.example.ui.components.BookEditDialog
import com.example.ui.components.BreadcrumbsBar
import com.example.ui.components.CameraBarcodeScannerDialog
import com.example.ui.components.LowStockTag
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.RedError
import com.example.ui.theme.RedErrorContainer
import com.example.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksCatalogScreen(
    currentUser: AppUser,
    viewModel: InventoryViewModel
) {
    val allBooks by viewModel.allBooks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStage by viewModel.selectedStage.collectAsState()
    val filterLowStockOnly by viewModel.filterLowStockOnly.collectAsState()

    var showBookDialog by remember { mutableStateOf(false) }
    var bookBeingEdited by remember { mutableStateOf<Book?>(null) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    var showScanner by remember { mutableStateOf(false) }

    // Filter books based on search, stage, and low-stock filter
    val filteredBooks = allBooks.filter { book ->
        val matchesSearch = searchQuery.isBlank() ||
                book.titleAr.contains(searchQuery, ignoreCase = true) ||
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.barcode.contains(searchQuery, ignoreCase = true) ||
                book.subject.contains(searchQuery, ignoreCase = true)

        val matchesStage = selectedStage == null || book.stage == selectedStage

        val matchesLowStock = !filterLowStockOnly || book.isLowStock

        matchesSearch && matchesStage && matchesLowStock
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("books_catalog_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Breadcrumbs
            item {
                BreadcrumbsBar(items = listOf("لوحة المؤشرات", "فهرس ومخزون الكتب المدرسية (Books Catalog)"))
            }

            // 2. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("بحث بالعنوان، الباركود، أو المادة الدراسية...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryTeal)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                            IconButton(
                                onClick = { showScanner = true },
                                modifier = Modifier.testTag("catalog_scan_barcode_icon")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "مسح باركود بالماسح الضوئي",
                                    tint = PrimaryTeal
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 3. Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedStage == null,
                        onClick = { viewModel.setSelectedStage(null) },
                        label = { Text("الكل") },
                        modifier = Modifier.testTag("filter_all_stage")
                    )

                    FilterChip(
                        selected = selectedStage == "PRIMARY",
                        onClick = { viewModel.setSelectedStage(if (selectedStage == "PRIMARY") null else "PRIMARY") },
                        label = { Text("ابتدائي") }
                    )

                    FilterChip(
                        selected = selectedStage == "PREPARATORY",
                        onClick = { viewModel.setSelectedStage(if (selectedStage == "PREPARATORY") null else "PREPARATORY") },
                        label = { Text("إعدادي") }
                    )

                    FilterChip(
                        selected = selectedStage == "SECONDARY",
                        onClick = { viewModel.setSelectedStage(if (selectedStage == "SECONDARY") null else "SECONDARY") },
                        label = { Text("ثانوي") }
                    )

                    FilterChip(
                        selected = filterLowStockOnly,
                        onClick = { viewModel.toggleFilterLowStock() },
                        label = { Text("عجز حرج فقط") },
                        leadingIcon = {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            // 4. Header with total count & Add button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قائمة الكتب المدرسية (${filteredBooks.size} كتاب):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (currentUser.role.canManageBooks() || currentUser.role.canCreateInbound()) {
                        Button(
                            onClick = {
                                bookBeingEdited = null
                                showBookDialog = true
                            },
                            modifier = Modifier.testTag("add_book_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة كتاب")
                        }
                    }
                }
            }

            // 5. Books List or Empty State
            if (filteredBooks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لم يتم العثور على كتب مطابقة للبحث",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "جرب تغيير مصطلح البحث أو إزالة المرشحات النشطة.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(filteredBooks, key = { it.id }) { book ->
                    BookCatalogItemCard(
                        book = book,
                        canEdit = currentUser.role.canManageBooks() || currentUser.role == com.example.data.model.UserRole.ADMIN,
                        canDelete = currentUser.role.canDelete(),
                        onEdit = {
                            bookBeingEdited = book
                            showBookDialog = true
                        },
                        onDelete = {
                            bookToDelete = book
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showScanner = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("catalog_scan_fab"),
            containerColor = PrimaryTeal,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "مسح باركود"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("مسح الباركود", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add / Edit Dialog
    if (showBookDialog) {
        BookEditDialog(
            bookToEdit = bookBeingEdited,
            onSaveBook = { savedBook ->
                viewModel.saveBook(savedBook) {
                    showBookDialog = false
                    bookBeingEdited = null
                }
            },
            onDismiss = {
                showBookDialog = false
                bookBeingEdited = null
            }
        )
    }

    // Confirm Delete Dialog
    if (bookToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text("تأكيد حذف الكتاب") },
            text = {
                Text("هل أنت متأكد من رغبتك في حذف كتاب '${bookToDelete?.titleAr}' من الفهرس العام؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        bookToDelete?.let { viewModel.deleteBook(it) }
                        bookToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { bookToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showScanner) {
        CameraBarcodeScannerDialog(
            booksCatalog = allBooks,
            onBarcodeScanned = { scannedCode ->
                viewModel.setSearchQuery(scannedCode)
                showScanner = false
            },
            onDismiss = { showScanner = false }
        )
    }
}

@Composable
fun BookCatalogItemCard(
    book: Book,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCritical = book.isLowStock
    val progress = if (book.minStockLevel > 0) {
        (book.currentStock.toFloat() / (book.minStockLevel * 2f)).coerceIn(0f, 1f)
    } else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_item_${book.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCritical) RedErrorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.titleAr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryTeal.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${book.stage} • الصف ${book.grade}",
                                color = PrimaryTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = book.academicYear,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (isCritical) {
                            LowStockTag(currentStock = book.currentStock, minStock = book.minStockLevel)
                        }
                    }
                }

                // Action buttons
                Row {
                    if (canEdit) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = PrimaryTeal)
                        }
                    }
                    if (canDelete) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedError)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock Details Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الرصيد الفعلي الحالي:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${book.currentStock} نسخة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) RedError else GreenSuccess
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "حد الأمان الأدنى: ${book.minStockLevel} نسخة",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "الباركود: ${book.barcode}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stock level visual indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCritical) RedError else GreenSuccess,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
