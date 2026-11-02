package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.GeminiAssistantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AiAssistantViewModel(private val repository: GeminiAssistantRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                text = "مرحباً بك في المساعد الذكي لمخازن الكتب المدرسية (مدعوم بنموذج gemini-3.1-flash-lite فائق السرعة). يمكنك الاستفسار عن نواقص الكتب، خطة التوزيع للمدارس، أو طلب تحليل فوري للمخزون."
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage(prompt: String? = null) {
        val query = (prompt ?: _inputText.value).trim()
        if (query.isBlank() || _isLoading.value) return

        _inputText.value = ""
        val currentList = _messages.value.toMutableList()
        currentList.add(ChatMessage("user", query))
        _messages.value = currentList
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.askAssistant(query)
                val updated = _messages.value.toMutableList()
                updated.add(ChatMessage("ai", response))
                _messages.value = updated
            } catch (e: Exception) {
                val updated = _messages.value.toMutableList()
                updated.add(ChatMessage("ai", "عذراً، حدث خطأ: ${e.localizedMessage}"))
                _messages.value = updated
            } finally {
                _isLoading.value = false
            }
        }
    }
}
