package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Book
import com.example.data.model.InventoryTransaction
import com.example.data.model.School
import com.example.data.model.SyncLog
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryRepository(private val database: AppDatabase) {
    private val bookDao = database.bookDao()
    private val transactionDao = database.transactionDao()
    private val schoolDao = database.schoolDao()
    private val syncLogDao = database.syncLogDao()

    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val lowStockBooks: Flow<List<Book>> = bookDao.getLowStockBooks()
    val allTransactions: Flow<List<InventoryTransaction>> = transactionDao.getAllTransactions()
    val recentTransactions: Flow<List<InventoryTransaction>> = transactionDao.getRecentTransactions(15)
    val allSchools: Flow<List<School>> = schoolDao.getAllSchools()
    val syncLogs: Flow<List<SyncLog>> = syncLogDao.getAllLogs()
    val unsyncedCount: Flow<Int> = transactionDao.getUnsyncedTransactionsCount()
    val totalBooksCount: Flow<Int> = bookDao.getBooksCount()
    val totalPhysicalStock: Flow<Int?> = bookDao.getTotalPhysicalStock()
    val lowStockCount: Flow<Int> = bookDao.getLowStockCount()

    fun getTodayTransactionsCount(): Flow<Int> {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return transactionDao.getTodayTransactionsCount(calendar.timeInMillis)
    }

    suspend fun getBookById(id: Long): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookById(id)
    }

    suspend fun getBookByBarcode(barcode: String): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookByBarcode(barcode)
    }

    suspend fun getTransactionWithItems(txId: Long): TransactionWithItems? = withContext(Dispatchers.IO) {
        val tx = transactionDao.getTransactionById(txId) ?: return@withContext null
        val items = transactionDao.getItemsForTransaction(txId)
        TransactionWithItems(tx, items)
    }

    suspend fun insertOrUpdateBook(book: Book): Long = withContext(Dispatchers.IO) {
        if (book.id == 0L) {
            bookDao.insertBook(book.copy(isSynced = false, lastUpdated = System.currentTimeMillis()))
        } else {
            bookDao.updateBook(book.copy(isSynced = false, lastUpdated = System.currentTimeMillis()))
            book.id
        }
    }

    suspend fun deleteBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.deleteBook(book)
    }

    /**
     * Inbound Transaction: Automatically increases inventory stock
     */
    suspend fun processInboundTransaction(
        source: String,
        academicYear: String,
        officerName: String,
        notes: String,
        items: List<Pair<Book, Int>> // Book and Quantity
    ): Result<InventoryTransaction> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("يجب اختيار كتاب واحد على الأقل للتوريد"))
        }

        val totalQty = items.sumOf { it.second }
        val dateStr = SimpleDateFormat("yyMMdd-HHmmss", Locale.US).format(Date())
        val txNumber = "TX-IN-$dateStr"

        val transaction = InventoryTransaction(
            transactionNumber = txNumber,
            type = "INBOUND",
            sourceOrDestination = source.ifBlank { "توريد يدوي / مخازن مركزية" },
            academicYear = academicYear,
            officerName = officerName,
            notes = notes,
            timestamp = System.currentTimeMillis(),
            totalQuantity = totalQty,
            isSynced = false
        )

        val txId = transactionDao.insertTransaction(transaction)

        val transactionItems = items.map { (book, quantity) ->
            // Increase stock
            val newStock = book.currentStock + quantity
            bookDao.updateStock(book.id, newStock)

            TransactionItem(
                transactionId = txId,
                bookId = book.id,
                bookTitle = "${book.titleAr} (${book.title})",
                quantity = quantity,
                unitPrice = book.unitCost
            )
        }
        transactionDao.insertTransactionItems(transactionItems)

        Result.success(transaction.copy(id = txId))
    }

    /**
     * Outbound Transaction: Strict validation to prevent negative stock and decreases inventory
     */
    suspend fun processOutboundTransaction(
        destinationSchool: String,
        schoolId: Long?,
        academicYear: String,
        officerName: String,
        notes: String,
        items: List<Pair<Book, Int>> // Book and Requested Quantity
    ): Result<InventoryTransaction> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("يجب اختيار كتاب واحد على الأقل للصرف والتوزيع"))
        }

        // Strict Inventory Validation: Check current stock
        for ((book, requestedQty) in items) {
            val freshBook = bookDao.getBookById(book.id) ?: return@withContext Result.failure(
                IllegalStateException("الكتاب غير موجود في السجل: ${book.titleAr}")
            )

            if (requestedQty <= 0) {
                return@withContext Result.failure(
                    IllegalArgumentException("الكمية المطلوبة للكتاب '${book.titleAr}' يجب أن تكون أكبر من صفر")
                )
            }

            if (freshBook.currentStock < requestedQty) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "الرصيد غير كافٍ للكتاب '${freshBook.titleAr}'. الرصيد المتاح حالياً: ${freshBook.currentStock} نسخة، والمطلوب: $requestedQty نسخة."
                    )
                )
            }
        }

        val totalQty = items.sumOf { it.second }
        val dateStr = SimpleDateFormat("yyMMdd-HHmmss", Locale.US).format(Date())
        val txNumber = "TX-OUT-$dateStr"

        val transaction = InventoryTransaction(
            transactionNumber = txNumber,
            type = "OUTBOUND",
            sourceOrDestination = destinationSchool,
            schoolId = schoolId,
            academicYear = academicYear,
            officerName = officerName,
            notes = notes,
            timestamp = System.currentTimeMillis(),
            totalQuantity = totalQty,
            isSynced = false
        )

        val txId = transactionDao.insertTransaction(transaction)

        val transactionItems = items.map { (book, quantity) ->
            val freshBook = bookDao.getBookById(book.id)!!
            val newStock = freshBook.currentStock - quantity
            // Decrease stock
            bookDao.updateStock(book.id, newStock)

            TransactionItem(
                transactionId = txId,
                bookId = book.id,
                bookTitle = "${book.titleAr} (${book.title})",
                quantity = quantity,
                unitPrice = book.unitCost
            )
        }
        transactionDao.insertTransactionItems(transactionItems)

        Result.success(transaction.copy(id = txId))
    }
}
