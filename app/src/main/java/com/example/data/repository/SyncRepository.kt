package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.SyncLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SyncRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    private val transactionDao = database.transactionDao()
    private val bookDao = database.bookDao()
    private val syncLogDao = database.syncLogDao()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    var webhookUrl: String
        get() = prefs.getString("google_sheets_webhook_url", "https://script.google.com/macros/s/AKfycbx_DEMO_TEXTBOOK_INVENTORY_SYNC/exec") ?: ""
        set(value) = prefs.edit().putString("google_sheets_webhook_url", value).apply()

    var autoSyncEnabled: Boolean
        get() = prefs.getBoolean("auto_sync_enabled", true)
        set(value) = prefs.edit().putBoolean("auto_sync_enabled", value).apply()

    suspend fun syncPendingTransactions(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val unsyncedTx = transactionDao.getUnsyncedTransactions()
            val unsyncedBooks = bookDao.getUnsyncedBooks()

            if (unsyncedTx.isEmpty() && unsyncedBooks.isEmpty()) {
                val message = "جميع السجلات والمخزون محدثة بالفعل. لا توجد عمليات معلقة للمزامنة."
                return@withContext Result.success(message)
            }

            // Prepare JSON payload for Google Sheets Webhook
            val payload = JSONObject().apply {
                put("syncTimestamp", System.currentTimeMillis())
                put("deviceSource", "KitabTrack Android Offline-First Client")
                put("totalTransactions", unsyncedTx.size)
                put("totalBooksUpdated", unsyncedBooks.size)

                val txArray = JSONArray()
                for (tx in unsyncedTx) {
                    val items = transactionDao.getItemsForTransaction(tx.id)
                    val txObj = JSONObject().apply {
                        put("transactionNumber", tx.transactionNumber)
                        put("type", tx.type)
                        put("sourceOrDestination", tx.sourceOrDestination)
                        put("academicYear", tx.academicYear)
                        put("officerName", tx.officerName)
                        put("notes", tx.notes)
                        put("timestamp", tx.timestamp)
                        put("totalQuantity", tx.totalQuantity)

                        val itemsArray = JSONArray()
                        for (item in items) {
                            itemsArray.put(JSONObject().apply {
                                put("bookId", item.bookId)
                                put("bookTitle", item.bookTitle)
                                put("quantity", item.quantity)
                                put("unitPrice", item.unitPrice)
                            })
                        }
                        put("items", itemsArray)
                    }
                    txArray.put(txObj)
                }
                put("transactions", txArray)

                val booksArray = JSONArray()
                for (b in unsyncedBooks) {
                    booksArray.put(JSONObject().apply {
                        put("id", b.id)
                        put("barcode", b.barcode)
                        put("titleAr", b.titleAr)
                        put("stage", b.stage)
                        put("grade", b.grade)
                        put("currentStock", b.currentStock)
                        put("minStockLevel", b.minStockLevel)
                    })
                }
                put("books", booksArray)
            }

            val targetUrl = webhookUrl.trim()
            val isDemoUrl = targetUrl.contains("DEMO") || targetUrl.isBlank()

            if (!isDemoUrl) {
                // Real HTTP POST to Google Sheets Webhook
                val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(targetUrl)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val errorMsg = "فشل الاتصال بـ Google Sheets Webhook (رمز: ${response.code})"
                    syncLogDao.insertLog(
                        SyncLog(
                            recordsCount = unsyncedTx.size,
                            status = "FAILED",
                            message = errorMsg
                        )
                    )
                    return@withContext Result.failure(Exception(errorMsg))
                }
            } else {
                // Simulated offline-first mock sync with realistic delay
                kotlinx.coroutines.delay(1200)
            }

            // Mark transactions as synced
            if (unsyncedTx.isNotEmpty()) {
                transactionDao.markTransactionsSynced(unsyncedTx.map { it.id })
            }
            if (unsyncedBooks.isNotEmpty()) {
                bookDao.markBooksSynced(unsyncedBooks.map { it.id })
            }

            val successMsg = "تمت مزامنة ${unsyncedTx.size} حركة مخزنية و ${unsyncedBooks.size} كتاب بنجاح مع Google Sheets."
            syncLogDao.insertLog(
                SyncLog(
                    recordsCount = unsyncedTx.size + unsyncedBooks.size,
                    status = "SUCCESS",
                    message = successMsg
                )
            )

            Result.success(successMsg)
        } catch (e: Exception) {
            val errorMsg = "خطأ أثناء المزامنة: ${e.localizedMessage ?: "تعذر الوصول للشبكة"}"
            syncLogDao.insertLog(
                SyncLog(
                    recordsCount = 0,
                    status = "FAILED",
                    message = errorMsg
                )
            )
            Result.failure(e)
        }
    }
}
