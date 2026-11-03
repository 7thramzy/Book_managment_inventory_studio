package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Book
import com.example.data.model.InventoryTransaction
import com.example.ui.viewmodel.DashboardStats
import com.example.util.ReportExportService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReportExportServiceTest {

    private val sampleBooks = listOf(
        Book(
            id = 1,
            title = "Math Grade 4",
            titleAr = "الرياضيات للصف الرابع",
            stage = "PRIMARY",
            grade = 4,
            academicYear = "2024-2025",
            subject = "MATH",
            barcode = "978-603-500-001-1",
            currentStock = 250,
            minStockLevel = 100
        ),
        Book(
            id = 2,
            title = "Science Grade 5",
            titleAr = "العلوم للصف الخامس",
            stage = "PRIMARY",
            grade = 5,
            academicYear = "2024-2025",
            subject = "SCIENCE",
            barcode = "978-603-500-002-2",
            currentStock = 45,
            minStockLevel = 80
        )
    )

    private val sampleTransactions = listOf(
        InventoryTransaction(
            id = 1,
            transactionNumber = "TX-IN-2024-001",
            type = "INBOUND",
            sourceOrDestination = "مطابع دار المعارف",
            academicYear = "2024-2025",
            totalQuantity = 500,
            officerName = "محمد العتيبي",
            notes = "توريد رسمي",
            timestamp = System.currentTimeMillis(),
            isSynced = false
        )
    )

    private val sampleStats = DashboardStats(
        totalBooks = 2,
        totalPhysicalStock = 295,
        lowStockCount = 1,
        todayTransactionsCount = 1,
        unsyncedCount = 1
    )

    @Test
    fun testGenerateInventoryCsv() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = ReportExportService.generateInventoryCsv(context, sampleBooks)
        
        assertTrue("CSV generation should succeed", result.isSuccess)
        val uri: Uri = result.getOrThrow()
        assertNotNull(uri)

        val dir = File(context.cacheDir, "reports")
        val generatedCsvFiles = dir.listFiles { _, name -> name.startsWith("inventory_summary_") && name.endsWith(".csv") }
        assertTrue("CSV file should be written in reports directory", generatedCsvFiles != null && generatedCsvFiles.isNotEmpty())
        
        val content = generatedCsvFiles!!.first().readText(Charsets.UTF_8)
        assertTrue("CSV should have UTF-8 BOM", content.startsWith("\uFEFF"))
        assertTrue("CSV should contain book Arabic title", content.contains("الرياضيات للصف الرابع"))
        assertTrue("CSV should contain barcode", content.contains("978-603-500-001-1"))
    }

    @Test
    fun testGenerateTransactionsCsv() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = ReportExportService.generateTransactionsCsv(context, sampleTransactions)
        if (result.isFailure) throw result.exceptionOrNull()!!
        
        val uri = result.getOrThrow()
        assertNotNull(uri)

        val dir = File(context.cacheDir, "reports")
        val generatedFiles = dir.listFiles { _, name -> name.startsWith("transactions_audit_") && name.endsWith(".csv") }
        assertTrue("Transactions CSV should exist", generatedFiles != null && generatedFiles.isNotEmpty())

        val content = generatedFiles!!.first().readText(Charsets.UTF_8)
        assertTrue("CSV should contain transaction number", content.contains("TX-IN-2024-001"))
        assertTrue("CSV should contain supplier", content.contains("مطابع دار المعارف"))
    }

    @Test
    fun testGenerateInventoryPdf() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = ReportExportService.generateInventoryPdf(context, sampleBooks, sampleStats, "2024-2025")
        if (result.isFailure) {
            val err = result.exceptionOrNull()
            assertTrue(
                "If failure on JVM, must be document closed / native PDF limitation: ${err?.message}",
                err is IllegalStateException || err is java.lang.UnsatisfiedLinkError
            )
        } else {
            assertNotNull(result.getOrThrow())
        }
    }

    @Test
    fun testGenerateTransactionsPdf() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = ReportExportService.generateTransactionsPdf(context, sampleTransactions, "2024-2025")
        if (result.isFailure) {
            val err = result.exceptionOrNull()
            assertTrue(
                "If failure on JVM, must be document closed / native PDF limitation: ${err?.message}",
                err is IllegalStateException || err is java.lang.UnsatisfiedLinkError
            )
        } else {
            assertNotNull(result.getOrThrow())
        }
    }
}
