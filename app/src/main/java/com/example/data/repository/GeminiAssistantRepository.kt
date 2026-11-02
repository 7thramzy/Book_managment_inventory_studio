package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAssistantRepository(private val database: AppDatabase) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val modelName = "gemini-3.1-flash-lite"

    /**
     * Sends request to Gemini 3.1 Flash Lite with real warehouse context.
     * Guaranteed low latency with offline fallback if offline.
     */
    suspend fun askAssistant(userPrompt: String): String = withContext(Dispatchers.IO) {
        val books = database.bookDao().getAllBooks().firstOrNull() ?: emptyList()
        val lowStock = database.bookDao().getLowStockBooks().firstOrNull() ?: emptyList()
        val totalBooks = books.size
        val totalStock = books.sumOf { it.currentStock }

        // Context summary for grounding
        val contextSummary = buildString {
            appendLine("بيانات مخزن الكتب المدرسية الحالي:")
            appendLine("- إجمالي عناوين الكتب في الفهرس: $totalBooks")
            appendLine("- إجمالي الرصيد الفعلي في المستودع: $totalStock نسخة")
            appendLine("- عدد الكتب ذات الرصيد الحرج (تحت حد الأمان): ${lowStock.size}")
            if (lowStock.isNotEmpty()) {
                appendLine("الكتب ذات النقص:")
                lowStock.forEach {
                    appendLine("• ${it.titleAr} (${it.stage} - الصف ${it.grade}): الرصيد الحالي ${it.currentStock} / الحد الأدنى ${it.minStockLevel}")
                }
            }
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // High quality local heuristic response
            return@withContext generateLocalInventoryAdvice(userPrompt, books, lowStock, totalBooks, totalStock)
        }

        try {
            val systemInstruction = """
                أنت "المساعد الذكي لمخازن الكتب المدرسية (KitabTrack AI)" المدعوم بنموذج gemini-3.1-flash-lite فائق السرعة ومنخفض الكمون.
                مهمتك: تقديم نصائح فورية دقيقة لأمناء المستودعات ومدققي الحسابات بأسلوب مهني وموجز باللغة العربية.
                اعتمد على بيانات المستودع الفعلية المرفقة بدقة واقترح حلولاً عملية للتوريد والتوزيع.
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstruction\n\n$contextSummary\n\nسؤال المستخدم: $userPrompt")
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("maxOutputTokens", 800)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotBlank()) {
                            return@withContext text
                        }
                    }
                }
            }

            Log.w("GeminiAssistant", "API call did not return text, falling back to local heuristic. Code: ${response.code}")
            generateLocalInventoryAdvice(userPrompt, books, lowStock, totalBooks, totalStock)
        } catch (e: Exception) {
            Log.e("GeminiAssistant", "Error during Gemini call, using offline mode", e)
            generateLocalInventoryAdvice(userPrompt, books, lowStock, totalBooks, totalStock)
        }
    }

    private fun generateLocalInventoryAdvice(
        prompt: String,
        allBooks: List<Book>,
        lowStock: List<Book>,
        totalBooks: Int,
        totalStock: Int
    ): String {
        return buildString {
            appendLine("🤖 [مساعد KitabTrack الذكي - استجابة سريعة]:")
            appendLine()
            if (prompt.contains("نقص") || prompt.contains("عجز") || prompt.contains("حرج") || prompt.contains("طلب") || prompt.contains("توريد")) {
                if (lowStock.isEmpty()) {
                    appendLine("✅ الوضع المخزني ممتاز! جميع الكتب المدرسية ($totalBooks عنوان) تتجاوز الحدود الدنيا للأمان المخزني.")
                } else {
                    appendLine("⚠️ تنبيهات المخزون الحرج (${lowStock.size} كتب تحتاج توريد فوري):")
                    lowStock.forEachIndexed { index, book ->
                        val deficit = book.minStockLevel - book.currentStock
                        appendLine("${index + 1}. ${book.titleAr} (الصف ${book.grade} - ${book.stage}):")
                        appendLine("   • الرصيد الحالي: ${book.currentStock} نسخة (الحد الأدنى: ${book.minStockLevel})")
                        appendLine("   • العجز المباشر: $deficit نسخة | الكمية المقترحة للتوريد: ${deficit + 200} نسخة لتغطية الفصل الدراسي.")
                    }
                }
            } else if (prompt.contains("تقرير") || prompt.contains("تدقيق") || prompt.contains("ملخص")) {
                appendLine("📊 ملخص التدقيق والجاهزية:")
                appendLine("• إجمالي العناوين المسجلة: $totalBooks كتاب")
                appendLine("• إجمالي المخزون الفعلي: $totalStock نسخة في المستودع")
                appendLine("• نسبة تغطية الأمان المخزني: ${if (totalBooks > 0) ((totalBooks - lowStock.size) * 100 / totalBooks) else 100}%")
                appendLine("• التوصية: التنسيق مع إدارة التوزيع لإرسال الحصص المتبقية إلى المدارس الابتدائية والإعدادية بعد استكمال النواقص.")
            } else {
                appendLine("مستودع الكتب المدرسية يحتوي حالياً على $totalStock نسخة موزعة على $totalBooks عنوان تعليمي.")
                if (lowStock.isNotEmpty()) {
                    appendLine("يوجد ${lowStock.size} عنوان تحت حد الأمان يُوصى بإصدار إذن توريد عاجل لها.")
                } else {
                    appendLine("كافة الأرصدة كافية ومطابقة لمعايير الاستهلاك للعام الدراسي الحالي.")
                }
            }
        }
    }
}
