package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Book
import com.example.data.model.InventoryTransaction
import com.example.ui.viewmodel.DashboardStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExportService {

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getFileUri(context: Context, file: File): Uri {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Uri.fromFile(file)
        }
    }

    /**
     * Generates a printable, formatted PDF of the current inventory audit summary.
     */
    suspend fun generateInventoryPdf(
        context: Context,
        books: List<Book>,
        stats: DashboardStats,
        academicYear: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595 // Standard A4 points
            val pageHeight = 842

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(0, 111, 136) // #006F88 Primary Teal
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }

            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 10f
                textAlign = Paint.Align.RIGHT
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(0, 79, 98)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }

            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 8.5f
                textAlign = Paint.Align.RIGHT
            }

            val alertPaint = Paint().apply {
                color = Color.rgb(220, 38, 38)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }

            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            val bgHeaderPaint = Paint().apply {
                color = Color.rgb(228, 236, 239)
            }

            // Header banner
            canvas.drawRect(20f, 20f, (pageWidth - 20).toFloat(), 75f, bgHeaderPaint)
            val currentDate = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date())

            canvas.drawText("KitabTrack - تقرير الجرد والمطابقة المخزنية للكتب المدرسية", (pageWidth - 30).toFloat(), 42f, titlePaint)
            canvas.drawText("العام الدراسي: $academicYear | تاريخ التقرير: $currentDate", (pageWidth - 30).toFloat(), 58f, subtitlePaint)
            canvas.drawText("إجمالي العناوين: ${stats.totalBooks} | إجمالي الرصيد: ${stats.totalPhysicalStock} نسخة | نواقص حرجة: ${stats.lowStockCount}", (pageWidth - 30).toFloat(), 70f, subtitlePaint)

            // Table Header
            var yPos = 100f
            canvas.drawRect(20f, yPos - 12f, (pageWidth - 20).toFloat(), yPos + 6f, bgHeaderPaint)

            canvas.drawText("م", 40f, yPos, headerPaint)
            canvas.drawText("الباركود", 100f, yPos, headerPaint)
            canvas.drawText("المرحلة والصف", 180f, yPos, headerPaint)
            canvas.drawText("الحد الأدنى", 230f, yPos, headerPaint)
            canvas.drawText("الرصيد", 280f, yPos, headerPaint)
            canvas.drawText("الحالة", 340f, yPos, headerPaint)
            canvas.drawText("عنوان الكتاب المدرسي", (pageWidth - 30).toFloat(), yPos, headerPaint)

            yPos += 14f

            books.take(38).forEachIndexed { index, book ->
                val isDeficit = book.isLowStock
                val statusText = if (isDeficit) "عجز حرج" else "كافٍ ومستقر"

                if (index % 2 == 1) {
                    canvas.drawRect(20f, yPos - 10f, (pageWidth - 20).toFloat(), yPos + 4f, Paint().apply { color = Color.rgb(248, 250, 251) })
                }

                canvas.drawText("${index + 1}", 40f, yPos, bodyPaint)
                canvas.drawText(book.barcode.takeLast(9), 100f, yPos, bodyPaint)
                canvas.drawText("${book.stage.take(4)} ص${book.grade}", 180f, yPos, bodyPaint)
                canvas.drawText("${book.minStockLevel}", 230f, yPos, bodyPaint)
                canvas.drawText("${book.currentStock}", 280f, yPos, if (isDeficit) alertPaint else bodyPaint)
                canvas.drawText(statusText, 340f, yPos, if (isDeficit) alertPaint else bodyPaint)
                canvas.drawText(book.titleAr, (pageWidth - 30).toFloat(), yPos, bodyPaint)

                canvas.drawLine(20f, yPos + 4f, (pageWidth - 20).toFloat(), yPos + 4f, linePaint)
                yPos += 16f
            }

            // Footer
            canvas.drawText("تم إنشاء هذا المستند رسمياً من نظام KitabTrack للرقابة والتدقيق المخزني", (pageWidth - 30).toFloat(), (pageHeight - 25).toFloat(), subtitlePaint)

            pdfDoc.finishPage(page)

            val file = File(getReportsDir(context), "inventory_audit_report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            pdfDoc.close()
            outputStream.flush()
            outputStream.close()

            Result.success(getFileUri(context, file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a printable, formatted PDF of transaction history (Inbound / Outbound audit log).
     */
    suspend fun generateTransactionsPdf(
        context: Context,
        transactions: List<InventoryTransaction>,
        academicYear: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(0, 111, 136)
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }

            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 9.5f
                textAlign = Paint.Align.RIGHT
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(0, 79, 98)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }

            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 8.5f
                textAlign = Paint.Align.RIGHT
            }

            val bgHeaderPaint = Paint().apply { color = Color.rgb(228, 236, 239) }
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

            // Header Banner
            canvas.drawRect(20f, 20f, (pageWidth - 20).toFloat(), 75f, bgHeaderPaint)
            val currentDate = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date())

            canvas.drawText("KitabTrack - سجل أذون التوريد والصرف المخزني (Transactions Audit Log)", (pageWidth - 30).toFloat(), 42f, titlePaint)
            canvas.drawText("العام الدراسي: $academicYear | تاريخ التصدير: $currentDate", (pageWidth - 30).toFloat(), 58f, subtitlePaint)
            canvas.drawText("إجمالي العمليات المسجلة: ${transactions.size} أذون رسمية", (pageWidth - 30).toFloat(), 70f, subtitlePaint)

            var yPos = 100f
            canvas.drawRect(20f, yPos - 12f, (pageWidth - 20).toFloat(), yPos + 6f, bgHeaderPaint)

            canvas.drawText("م", 40f, yPos, headerPaint)
            canvas.drawText("رقم الإذن", 110f, yPos, headerPaint)
            canvas.drawText("النوع", 160f, yPos, headerPaint)
            canvas.drawText("الكمية", 210f, yPos, headerPaint)
            canvas.drawText("التاريخ", 280f, yPos, headerPaint)
            canvas.drawText("المسؤول", 350f, yPos, headerPaint)
            canvas.drawText("المدرسة / المورد", (pageWidth - 30).toFloat(), yPos, headerPaint)

            yPos += 14f

            transactions.take(38).forEachIndexed { index, tx ->
                val isInbound = tx.type == "INBOUND"
                val typeText = if (isInbound) "توريد (+)" else "صرف (-)"
                val dateStr = SimpleDateFormat("MM/dd HH:mm", Locale.US).format(Date(tx.timestamp))

                canvas.drawText("${index + 1}", 40f, yPos, bodyPaint)
                canvas.drawText(tx.transactionNumber.takeLast(11), 110f, yPos, bodyPaint)
                canvas.drawText(typeText, 160f, yPos, Paint().apply {
                    color = if (isInbound) Color.rgb(13, 148, 136) else Color.rgb(0, 111, 136)
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                })
                canvas.drawText("${tx.totalQuantity}", 210f, yPos, bodyPaint)
                canvas.drawText(dateStr, 280f, yPos, bodyPaint)
                canvas.drawText(tx.officerName.substringBefore(" ("), 350f, yPos, bodyPaint)
                canvas.drawText(tx.sourceOrDestination, (pageWidth - 30).toFloat(), yPos, bodyPaint)

                canvas.drawLine(20f, yPos + 4f, (pageWidth - 20).toFloat(), yPos + 4f, linePaint)
                yPos += 16f
            }

            pdfDoc.finishPage(page)

            val file = File(getReportsDir(context), "transactions_audit_report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            pdfDoc.close()
            outputStream.flush()
            outputStream.close()

            Result.success(getFileUri(context, file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a CSV file containing the complete books catalog and inventory levels.
     * Encoded with UTF-8 BOM (\uFEFF) to open seamlessly in Microsoft Excel and Google Sheets.
     */
    suspend fun generateInventoryCsv(
        context: Context,
        books: List<Book>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val file = File(getReportsDir(context), "inventory_summary_${System.currentTimeMillis()}.csv")
            val writer = OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8)

            // UTF-8 BOM
            writer.write("\uFEFF")

            // CSV Header
            writer.write("الرقم,عنوان الكتاب (عربي),العنوان (إنجليزي),المرحلة التعليمية,الصف الدراسي,العام الدراسي,المادة,الباركود,الرصيد الفعلي,الحد الأدنى,حالة العجز,القيمة التقديرية\n")

            books.forEachIndexed { index, book ->
                val isDeficit = if (book.isLowStock) "عجز حرج" else "كافٍ"
                val line = "${index + 1},\"${book.titleAr}\",\"${book.title}\",\"${book.stage}\",${book.grade},\"${book.academicYear}\",\"${book.subject}\",\"${book.barcode}\",${book.currentStock},${book.minStockLevel},\"$isDeficit\",${book.unitCost}\n"
                writer.write(line)
            }

            writer.flush()
            writer.close()

            Result.success(getFileUri(context, file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a CSV audit report for all inventory transactions.
     */
    suspend fun generateTransactionsCsv(
        context: Context,
        transactions: List<InventoryTransaction>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val file = File(getReportsDir(context), "transactions_audit_${System.currentTimeMillis()}.csv")
            val writer = OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8)

            writer.write("\uFEFF")
            writer.write("م,رقم الحركة,النوع,المدرسة أو المورد,العام الدراسي,إجمالي عدد النسخ,المسؤول المصرح,التاريخ,حالة المزامنة السحابية,ملاحظات\n")

            transactions.forEachIndexed { index, tx ->
                val typeStr = if (tx.type == "INBOUND") "توريد واستلام (+)" else "صرف وتوزيع مدارس (-)"
                val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(tx.timestamp))
                val syncStr = if (tx.isSynced) "مُزامن مع Sheets" else "محلي (أوفلاين)"
                val notesSanitized = tx.notes.replace("\"", "\"\"")

                val line = "${index + 1},\"${tx.transactionNumber}\",\"$typeStr\",\"${tx.sourceOrDestination}\",\"${tx.academicYear}\",${tx.totalQuantity},\"${tx.officerName}\",\"$dateStr\",\"$syncStr\",\"$notesSanitized\"\n"
                writer.write(line)
            }

            writer.flush()
            writer.close()

            Result.success(getFileUri(context, file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Opens native Android sharing chooser for the generated PDF/CSV file.
     */
    fun shareReportFile(context: Context, uri: Uri, mimeType: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
