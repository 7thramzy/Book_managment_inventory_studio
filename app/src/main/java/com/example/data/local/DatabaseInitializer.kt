package com.example.data.local

import com.example.data.model.Book
import com.example.data.model.EducationalStage
import com.example.data.model.InventoryTransaction
import com.example.data.model.School
import com.example.data.model.SyncLog
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun seedIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val existingBooksCount = database.bookDao().getBooksCount().firstOrNull() ?: 0
        if (existingBooksCount > 0) return@withContext

        // 1. Seed Schools
        val sampleSchools = listOf(
            School(
                name = "Al-Amal Secondary Boys School",
                nameAr = "مدرسة الأمل الثانوية للبنين",
                stage = "SECONDARY",
                district = "إدارة مصر الجديدة التعليمية",
                contactPerson = "أ. محمود عبد الرحمن",
                phone = "01001234567"
            ),
            School(
                name = "Al-Noor Primary School",
                nameAr = "مدرسة النور الابتدائية المشتركة",
                stage = "PRIMARY",
                district = "إدارة المعادي التعليمية",
                contactPerson = "أ. فاطمة حسن",
                phone = "01123456789"
            ),
            School(
                name = "Al-Mustaqbal Preparatory Girls",
                nameAr = "مدرسة المستقبل الإعدادية بنات",
                stage = "PREPARATORY",
                district = "إدارة الدقي التعليمية",
                contactPerson = "أ. سهير مصطفى",
                phone = "01234567890"
            ),
            School(
                name = "Al-Tamayoz Language School",
                nameAr = "مدرسة التميز الرسمية للغات",
                stage = "SECONDARY",
                district = "إدارة مدينة نصر التعليمية",
                contactPerson = "د. كريم سامي",
                phone = "01098765432"
            ),
            School(
                name = "Al-Nahda Secondary Girls School",
                nameAr = "مدرسة النهضة الثانوية بنات",
                stage = "SECONDARY",
                district = "إدارة الهرم التعليمية",
                contactPerson = "أ. نادية إبراهيم",
                phone = "01155667788"
            ),
            School(
                name = "Nile Preparatory Boys School",
                nameAr = "مدرسة النيل الإعدادية للبنين",
                stage = "PREPARATORY",
                district = "إدارة شبرا التعليمية",
                contactPerson = "أ. حسام فوزي",
                phone = "01277889900"
            )
        )
        database.schoolDao().insertSchools(sampleSchools)

        // 2. Seed Realistic Textbooks
        val sampleBooks = listOf(
            Book(
                title = "Arabic Language - My Readings",
                titleAr = "اللغة العربية - لغتي الجميلة",
                stage = "PRIMARY",
                grade = 4,
                academicYear = "2025/2026",
                subject = "اللغة العربية",
                barcode = "978-977-02-4011-2",
                minStockLevel = 150,
                currentStock = 620,
                unitCost = 35.0,
                description = "كتاب الفصل الدراسي الأول - المنهج المطور الجديد",
                isSynced = true
            ),
            Book(
                title = "Primary Mathematics 4",
                titleAr = "الرياضيات - الصف الرابع الابتدائي",
                stage = "PRIMARY",
                grade = 4,
                academicYear = "2025/2026",
                subject = "الرياضيات",
                barcode = "978-977-02-4012-9",
                minStockLevel = 120,
                currentStock = 85, // LOW STOCK ALERT!
                unitCost = 42.0,
                description = "كتاب التلميذ ودليل الأنشطة الحسابية",
                isSynced = true
            ),
            Book(
                title = "Science & Discovery 4",
                titleAr = "العلوم واستكشاف الطبيعة 4",
                stage = "PRIMARY",
                grade = 4,
                academicYear = "2025/2026",
                subject = "العلوم",
                barcode = "978-977-02-4013-6",
                minStockLevel = 100,
                currentStock = 450,
                unitCost = 38.0,
                description = "التجارب المعملية والمفاهيم البيئية",
                isSynced = true
            ),
            Book(
                title = "Connect Plus English 4",
                titleAr = "اللغة الإنجليزية كونكت بلس 4",
                stage = "PRIMARY",
                grade = 4,
                academicYear = "2025/2026",
                subject = "اللغة الإنجليزية",
                barcode = "978-977-02-4014-3",
                minStockLevel = 150,
                currentStock = 90, // LOW STOCK ALERT!
                unitCost = 45.0,
                description = "Student Book and Workbook - Term 1",
                isSynced = true
            ),
            Book(
                title = "Social Studies - Egyptian Geography",
                titleAr = "الدراسات الاجتماعية - مصر ومحيطها",
                stage = "PRIMARY",
                grade = 5,
                academicYear = "2025/2026",
                subject = "الدراسات الاجتماعية",
                barcode = "978-977-02-5011-8",
                minStockLevel = 100,
                currentStock = 380,
                unitCost = 32.0,
                description = "تاريخ وجغرافيا البيئات المصرية",
                isSynced = true
            ),
            Book(
                title = "Preparatory Mathematics 1",
                titleAr = "الجبر والإحصاء - أولى إعدادي",
                stage = "PREPARATORY",
                grade = 7,
                academicYear = "2025/2026",
                subject = "الرياضيات",
                barcode = "978-977-02-7011-5",
                minStockLevel = 200,
                currentStock = 540,
                unitCost = 48.0,
                description = "كتاب التدريبات والمعادلات الجبرية",
                isSynced = true
            ),
            Book(
                title = "Preparatory Science 1",
                titleAr = "العلوم العامة - أولى إعدادي",
                stage = "PREPARATORY",
                grade = 7,
                academicYear = "2025/2026",
                subject = "العلوم",
                barcode = "978-977-02-7012-2",
                minStockLevel = 180,
                currentStock = 410,
                unitCost = 44.0,
                description = "المادة وخواصها والتفاعلات الكيميائية الأساسية",
                isSynced = true
            ),
            Book(
                title = "Preparatory English 3",
                titleAr = "اللغة الإنجليزية نيو هلو - ثالثة إعدادي",
                stage = "PREPARATORY",
                grade = 9,
                academicYear = "2025/2026",
                subject = "اللغة الإنجليزية",
                barcode = "978-977-02-9011-9",
                minStockLevel = 250,
                currentStock = 720,
                unitCost = 50.0,
                description = "شهادة إتمام التعليم الأساسي",
                isSynced = true
            ),
            Book(
                title = "General Chemistry 1",
                titleAr = "الكيمياء العامة - أولى ثانوي",
                stage = "SECONDARY",
                grade = 10,
                academicYear = "2025/2026",
                subject = "الكيمياء",
                barcode = "978-977-02-1011-1",
                minStockLevel = 180,
                currentStock = 340,
                unitCost = 55.0,
                description = "التركيب الذري والروابط الكيميائية والديناميكا الحرارية",
                isSynced = true
            ),
            Book(
                title = "General Physics 1",
                titleAr = "الفيزياء الميكانيكية - أولى ثانوي",
                stage = "SECONDARY",
                grade = 10,
                academicYear = "2025/2026",
                subject = "الفيزياء",
                barcode = "978-977-02-1012-8",
                minStockLevel = 180,
                currentStock = 120, // LOW STOCK ALERT!
                unitCost = 58.0,
                description = "الحركة الخطية والدائرية وقوانين نيوتن",
                isSynced = true
            ),
            Book(
                title = "Biology & Life Sciences",
                titleAr = "الأحياء والعلوم الحياتية - ثانية ثانوي",
                stage = "SECONDARY",
                grade = 11,
                academicYear = "2025/2026",
                subject = "الأحياء",
                barcode = "978-977-02-1111-4",
                minStockLevel = 160,
                currentStock = 490,
                unitCost = 52.0,
                description = "التغذية والهضم والتنفس في الكائنات الحية",
                isSynced = true
            ),
            Book(
                title = "Pure Mathematics - Calculus",
                titleAr = "الرياضيات البحتة - التفاضل والتكامل 3 ثانوي",
                stage = "SECONDARY",
                grade = 12,
                academicYear = "2025/2026",
                subject = "الرياضيات",
                barcode = "978-977-02-1211-7",
                minStockLevel = 300,
                currentStock = 650,
                unitCost = 65.0,
                description = "منهج الثانوية العامة - شعبة علمي رياضة",
                isSynced = true
            )
        )
        database.bookDao().insertBooks(sampleBooks)

        // 3. Seed Realistic Mixed Inbound & Outbound Transactions
        val now = System.currentTimeMillis()
        val oneDay = 86_400_000L

        // Inbound #1 (Ministry Batch 1)
        val inTx1 = InventoryTransaction(
            transactionNumber = "TX-IN-2025-001",
            type = "INBOUND",
            sourceOrDestination = "وزارة التربية والتعليم - مخازن الوزارة المركزية (طريق النصر)",
            academicYear = "2025/2026",
            officerName = "م. خالد الأنصاري",
            notes = "توريد الدفعة الأولى من كتب المرحلة الابتدائية والثانوية للعام الدراسي الجديد",
            timestamp = now - (3 * oneDay),
            totalQuantity = 2500,
            isSynced = true
        )
        val inTx1Id = database.transactionDao().insertTransaction(inTx1)
        database.transactionDao().insertTransactionItems(
            listOf(
                TransactionItem(transactionId = inTx1Id, bookId = 1, bookTitle = "اللغة العربية - لغتي الجميلة", quantity = 800, unitPrice = 35.0),
                TransactionItem(transactionId = inTx1Id, bookId = 3, bookTitle = "العلوم واستكشاف الطبيعة 4", quantity = 700, unitPrice = 38.0),
                TransactionItem(transactionId = inTx1Id, bookId = 9, bookTitle = "الكيمياء العامة - أولى ثانوي", quantity = 500, unitPrice = 55.0),
                TransactionItem(transactionId = inTx1Id, bookId = 12, bookTitle = "الرياضيات البحتة - التفاضل والتكامل 3 ثانوي", quantity = 500, unitPrice = 65.0)
            )
        )

        // Inbound #2 (Press Delivery)
        val inTx2 = InventoryTransaction(
            transactionNumber = "TX-IN-2025-002",
            type = "INBOUND",
            sourceOrDestination = "مطابع الأهرام للكتب المدرسية - إذن توريد 4821",
            academicYear = "2025/2026",
            officerName = "م. خالد الأنصاري",
            notes = "شحنة كتب المرحلة الإعدادية واللغة الإنجليزية",
            timestamp = now - (2 * oneDay),
            totalQuantity = 1800,
            isSynced = true
        )
        val inTx2Id = database.transactionDao().insertTransaction(inTx2)
        database.transactionDao().insertTransactionItems(
            listOf(
                TransactionItem(transactionId = inTx2Id, bookId = 6, bookTitle = "الجبر والإحصاء - أولى إعدادي", quantity = 700, unitPrice = 48.0),
                TransactionItem(transactionId = inTx2Id, bookId = 7, bookTitle = "العلوم العامة - أولى إعدادي", quantity = 500, unitPrice = 44.0),
                TransactionItem(transactionId = inTx2Id, bookId = 8, bookTitle = "اللغة الإنجليزية نيو هلو - ثالثة إعدادي", quantity = 600, unitPrice = 50.0)
            )
        )

        // Outbound #1 (Distribution to Al-Amal Secondary School)
        val outTx1 = InventoryTransaction(
            transactionNumber = "TX-OUT-2025-001",
            type = "OUTBOUND",
            sourceOrDestination = "مدرسة الأمل الثانوية للبنين",
            schoolId = 1,
            academicYear = "2025/2026",
            officerName = "أحمد عبد الله (أمين المستودع)",
            notes = "صرف حصة مدرسة الأمل الثانوية للفصل الدراسي الأول - إيصال استلام رقم 1024",
            timestamp = now - oneDay,
            totalQuantity = 320,
            isSynced = true
        )
        val outTx1Id = database.transactionDao().insertTransaction(outTx1)
        database.transactionDao().insertTransactionItems(
            listOf(
                TransactionItem(transactionId = outTx1Id, bookId = 9, bookTitle = "الكيمياء العامة - أولى ثانوي", quantity = 160, unitPrice = 55.0),
                TransactionItem(transactionId = outTx1Id, bookId = 10, bookTitle = "الفيزياء الميكانيكية - أولى ثانوي", quantity = 160, unitPrice = 58.0)
            )
        )

        // Outbound #2 (Distribution to Al-Noor Primary School - Recent Today!)
        val outTx2 = InventoryTransaction(
            transactionNumber = "TX-OUT-2025-002",
            type = "OUTBOUND",
            sourceOrDestination = "مدرسة النور الابتدائية المشتركة",
            schoolId = 2,
            academicYear = "2025/2026",
            officerName = "أحمد عبد الله (أمين المستودع)",
            notes = "صرف كتب الصف الرابع الابتدائي - استلام مندوب المدرسة أ. فاطمة حسن",
            timestamp = now - (3 * 3600_000L),
            totalQuantity = 430,
            isSynced = false // Unsynced to demonstrate offline queue!
        )
        val outTx2Id = database.transactionDao().insertTransaction(outTx2)
        database.transactionDao().insertTransactionItems(
            listOf(
                TransactionItem(transactionId = outTx2Id, bookId = 1, bookTitle = "اللغة العربية - لغتي الجميلة", quantity = 180, unitPrice = 35.0),
                TransactionItem(transactionId = outTx2Id, bookId = 3, bookTitle = "العلوم واستكشاف الطبيعة 4", quantity = 250, unitPrice = 38.0)
            )
        )

        // 4. Initial Sync Log
        database.syncLogDao().insertLog(
            SyncLog(
                timestamp = now - (2 * oneDay),
                recordsCount = 3,
                status = "SUCCESS",
                message = "Synced initial batches TX-IN-2025-001, TX-IN-2025-002, and TX-OUT-2025-001 to Google Sheets Webhook"
            )
        )
    }
}
