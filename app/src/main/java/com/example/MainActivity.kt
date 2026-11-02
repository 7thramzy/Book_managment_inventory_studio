package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.repository.GeminiAssistantRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.SyncRepository
import com.example.ui.MainAppScaffold
import com.example.ui.theme.KitabTrackTheme
import com.example.ui.viewmodel.AiAssistantViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.SyncViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val inventoryRepository by lazy { InventoryRepository(database) }
    private val syncRepository by lazy { SyncRepository(this, database) }
    private val geminiRepository by lazy { GeminiAssistantRepository(database) }

    private val authViewModel: AuthViewModel by viewModels()

    private val inventoryViewModel: InventoryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InventoryViewModel(inventoryRepository) as T
            }
        }
    }

    private val syncViewModel: SyncViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SyncViewModel(syncRepository) as T
            }
        }
    }

    private val aiViewModel: AiAssistantViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AiAssistantViewModel(geminiRepository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Seed realistic demo textbooks, schools, and initial transactions
        lifecycleScope.launch {
            DatabaseInitializer.seedIfEmpty(database)
        }

        setContent {
            KitabTrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppScaffold(
                        authViewModel = authViewModel,
                        inventoryViewModel = inventoryViewModel,
                        syncViewModel = syncViewModel,
                        aiViewModel = aiViewModel
                    )
                }
            }
        }
    }
}
