package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.InventoryTransaction
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM inventory_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<InventoryTransaction>>

    @Query("SELECT * FROM inventory_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): InventoryTransaction?

    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId")
    suspend fun getItemsForTransaction(txId: Long): List<TransactionItem>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId")
    fun getItemsForTransactionFlow(txId: Long): Flow<List<TransactionItem>>

    @Query("SELECT * FROM inventory_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<InventoryTransaction>>

    @Query("SELECT COUNT(*) FROM inventory_transactions WHERE timestamp >= :startOfDay")
    fun getTodayTransactionsCount(startOfDay: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: InventoryTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItem>)

    @Query("SELECT * FROM inventory_transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<InventoryTransaction>

    @Query("UPDATE inventory_transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markTransactionsSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM inventory_transactions WHERE isSynced = 0")
    fun getUnsyncedTransactionsCount(): Flow<Int>
}
