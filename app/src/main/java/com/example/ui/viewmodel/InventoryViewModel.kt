package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Book
import com.example.data.model.InventoryTransaction
import com.example.data.model.School
import com.example.data.model.TransactionWithItems
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalBooks: Int = 0,
    val totalPhysicalStock: Int = 0,
    val lowStockCount: Int = 0,
    val todayTransactionsCount: Int = 0,
    val unsyncedCount: Int = 0
)

class InventoryViewModel(private val repository: InventoryRepository) : ViewModel() {

    val allBooks = repository.allBooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val lowStockBooks = repository.lowStockBooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentTransactions = repository.recentTransactions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allTransactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSchools = repository.allSchools.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val unsyncedCount = repository.unsyncedCount.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val stats: StateFlow<DashboardStats> = combine(
        repository.totalBooksCount,
        repository.totalPhysicalStock,
        repository.lowStockCount,
        repository.getTodayTransactionsCount(),
        repository.unsyncedCount
    ) { booksCount, physicalStock, lowCount, todayTx, unsynced ->
        DashboardStats(
            totalBooks = booksCount,
            totalPhysicalStock = physicalStock ?: 0,
            lowStockCount = lowCount,
            todayTransactionsCount = todayTx,
            unsyncedCount = unsynced
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, DashboardStats())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStage = MutableStateFlow<String?>(null) // null = all
    val selectedStage: StateFlow<String?> = _selectedStage.asStateFlow()

    private val _filterLowStockOnly = MutableStateFlow(false)
    val filterLowStockOnly: StateFlow<Boolean> = _filterLowStockOnly.asStateFlow()

    private val _selectedAcademicYear = MutableStateFlow("2025/2026")
    val selectedAcademicYear: StateFlow<String> = _selectedAcademicYear.asStateFlow()

    private val _activeTransactionDetail = MutableStateFlow<TransactionWithItems?>(null)
    val activeTransactionDetail: StateFlow<TransactionWithItems?> = _activeTransactionDetail.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedStage(stage: String?) {
        _selectedStage.value = stage
    }

    fun toggleFilterLowStock() {
        _filterLowStockOnly.value = !_filterLowStockOnly.value
    }

    fun setAcademicYear(year: String) {
        _selectedAcademicYear.value = year
    }

    fun dismissMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun loadTransactionDetail(id: Long) {
        viewModelScope.launch {
            _activeTransactionDetail.value = repository.getTransactionWithItems(id)
        }
    }

    fun clearActiveTransactionDetail() {
        _activeTransactionDetail.value = null
    }

    fun saveBook(book: Book, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                repository.insertOrUpdateBook(book)
                _successMessage.value = "تم حفظ بيانات الكتاب بنجاح: ${book.titleAr}"
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطأ أثناء حفظ الكتاب: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            try {
                repository.deleteBook(book)
                _successMessage.value = "تم حذف الكتاب من الفهرس: ${book.titleAr}"
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف الكتاب: ${e.localizedMessage}"
            }
        }
    }

    fun submitInboundTransaction(
        source: String,
        officerName: String,
        notes: String,
        items: List<Pair<Book, Int>>,
        onSuccess: (InventoryTransaction) -> Unit
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.processInboundTransaction(
                source = source,
                academicYear = _selectedAcademicYear.value,
                officerName = officerName,
                notes = notes,
                items = items
            )
            _isProcessing.value = false
            result.onSuccess { tx ->
                _successMessage.value = "تم تسجيل إذن التوريد بنجاح (${tx.transactionNumber}) وزيادة المخزون بمقدار ${tx.totalQuantity} نسخة."
                onSuccess(tx)
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "فشلت عملية التوريد"
            }
        }
    }

    fun submitOutboundTransaction(
        school: School,
        officerName: String,
        notes: String,
        items: List<Pair<Book, Int>>,
        onSuccess: (InventoryTransaction) -> Unit
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.processOutboundTransaction(
                destinationSchool = school.nameAr,
                schoolId = school.id,
                academicYear = _selectedAcademicYear.value,
                officerName = officerName,
                notes = notes,
                items = items
            )
            _isProcessing.value = false
            result.onSuccess { tx ->
                _successMessage.value = "تم صرف الشحنة لمدرسة '${school.nameAr}' بنجاح وتحديث الرصيد فوراً."
                onSuccess(tx)
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "فشلت عملية الصرف"
            }
        }
    }
}
