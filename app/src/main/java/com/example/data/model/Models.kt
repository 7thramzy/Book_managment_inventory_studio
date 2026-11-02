package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole(val labelAr: String, val labelEn: String) {
    STOREKEEPER("أمين المستودع", "Storekeeper"),
    AUDITOR("مدقق ومراجع", "Auditor"),
    ADMIN("مدير النظام", "Admin");

    fun canManageBooks(): Boolean = this == ADMIN
    fun canCreateInbound(): Boolean = this == STOREKEEPER || this == ADMIN
    fun canCreateOutbound(): Boolean = this == STOREKEEPER || this == ADMIN
    fun canAudit(): Boolean = this == AUDITOR || this == ADMIN
    fun canConfigureSync(): Boolean = this == ADMIN
    fun canDelete(): Boolean = this == ADMIN
}

data class AppUser(
    val id: String,
    val username: String,
    val fullName: String,
    val fullNameAr: String,
    val role: UserRole,
    val pin: String
)

enum class EducationalStage(val titleAr: String, val titleEn: String) {
    PRIMARY("المرحلة الابتدائية", "Primary Stage"),
    PREPARATORY("المرحلة الإعدادية", "Preparatory Stage"),
    SECONDARY("المرحلة الثانوية", "Secondary Stage")
}

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["barcode"], unique = false),
        Index(value = ["academicYear", "stage", "grade"])
    ]
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val titleAr: String,
    val stage: String, // PRIMARY, PREPARATORY, SECONDARY
    val grade: Int, // 1 to 12
    val academicYear: String, // e.g. "2025/2026"
    val subject: String,
    val barcode: String,
    val minStockLevel: Int,
    val currentStock: Int,
    val unitCost: Double = 0.0,
    val description: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    val isLowStock: Boolean
        get() = currentStock <= minStockLevel
}

enum class TransactionType(val labelAr: String, val labelEn: String) {
    INBOUND("توريد واستلام", "Inbound Purchase"),
    OUTBOUND("صرف وتوزيع مدارس", "School Distribution")
}

@Entity(
    tableName = "inventory_transactions",
    indices = [
        Index(value = ["transactionNumber"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["isSynced"])
    ]
)
data class InventoryTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionNumber: String,
    val type: String, // INBOUND or OUTBOUND
    val sourceOrDestination: String, // School name or Supplier / Ministry
    val schoolId: Long? = null,
    val academicYear: String,
    val officerName: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val totalQuantity: Int = 0,
    val isSynced: Boolean = false
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = InventoryTransaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transactionId"]),
        Index(value = ["bookId"])
    ]
)
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val bookId: Long,
    val bookTitle: String,
    val quantity: Int,
    val unitPrice: Double = 0.0
)

data class TransactionWithItems(
    val transaction: InventoryTransaction,
    val items: List<TransactionItem>
)

@Entity(tableName = "schools")
data class School(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val nameAr: String,
    val stage: String,
    val district: String,
    val contactPerson: String,
    val phone: String
)

@Entity(tableName = "sync_logs")
data class SyncLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val recordsCount: Int,
    val status: String, // SUCCESS, FAILED, QUEUED
    val message: String
)
