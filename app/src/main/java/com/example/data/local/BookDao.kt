package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY grade ASC, titleAr ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE academicYear = :year ORDER BY grade ASC, titleAr ASC")
    fun getBooksByYear(year: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): Book?

    @Query("SELECT * FROM books WHERE barcode = :barcode LIMIT 1")
    suspend fun getBookByBarcode(barcode: String): Book?

    @Query("SELECT * FROM books WHERE currentStock <= minStockLevel ORDER BY currentStock ASC")
    fun getLowStockBooks(): Flow<List<Book>>

    @Query("SELECT COUNT(*) FROM books")
    fun getBooksCount(): Flow<Int>

    @Query("SELECT SUM(currentStock) FROM books")
    fun getTotalPhysicalStock(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM books WHERE currentStock <= minStockLevel")
    fun getLowStockCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Update
    suspend fun updateBook(book: Book)

    @Query("UPDATE books SET currentStock = :newStock, lastUpdated = :timestamp, isSynced = 0 WHERE id = :bookId")
    suspend fun updateStock(bookId: Long, newStock: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("SELECT * FROM books WHERE isSynced = 0")
    suspend fun getUnsyncedBooks(): List<Book>

    @Query("UPDATE books SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markBooksSynced(ids: List<Long>)
}
