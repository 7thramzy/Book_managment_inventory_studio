package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.AppUser
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    val availableUsers = listOf(
        AppUser(
            id = "usr_storekeeper",
            username = "storekeeper",
            fullName = "Ahmed Abdullah",
            fullNameAr = "أحمد عبد الله",
            role = UserRole.STOREKEEPER,
            pin = "1234"
        ),
        AppUser(
            id = "usr_auditor",
            username = "auditor",
            fullName = "Sarah Mansour",
            fullNameAr = "سارة منصور",
            role = UserRole.AUDITOR,
            pin = "5678"
        ),
        AppUser(
            id = "usr_admin",
            username = "admin",
            fullName = "Eng. Khaled Al-Ansari",
            fullNameAr = "م. خالد الأنصاري",
            role = UserRole.ADMIN,
            pin = "9999"
        )
    )

    private val _currentUser = MutableStateFlow<AppUser>(availableUsers[0]) // Default Storekeeper
    val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun switchUser(user: AppUser, enteredPin: String): Boolean {
        if (enteredPin == user.pin) {
            _currentUser.value = user
            _authError.value = null
            return true
        } else {
            _authError.value = "رمز PIN غير صحيح للمستخدم ${user.fullNameAr}"
            return false
        }
    }

    // Direct switch for quick role demo mode
    fun quickSwitchRole(role: UserRole) {
        val user = availableUsers.firstOrNull { it.role == role } ?: availableUsers[0]
        _currentUser.value = user
        _authError.value = null
    }

    fun clearError() {
        _authError.value = null
    }
}
