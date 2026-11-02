package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.SyncLog
import com.example.data.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SyncViewModel(private val repository: SyncRepository) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _webhookUrl = MutableStateFlow(repository.webhookUrl)
    val webhookUrl: StateFlow<String> = _webhookUrl.asStateFlow()

    fun updateWebhookUrl(newUrl: String) {
        _webhookUrl.value = newUrl
        repository.webhookUrl = newUrl
    }

    fun syncNow() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "جارٍ الاتصال ومزامنة الحركات مع Google Sheets Webhook..."
            _syncError.value = null

            val result = repository.syncPendingTransactions()
            _isSyncing.value = false
            result.onSuccess { msg ->
                _syncMessage.value = msg
            }.onFailure { err ->
                _syncError.value = err.message ?: "فشلت المزامنة"
                _syncMessage.value = null
            }
        }
    }

    fun clearMessages() {
        _syncMessage.value = null
        _syncError.value = null
    }
}
