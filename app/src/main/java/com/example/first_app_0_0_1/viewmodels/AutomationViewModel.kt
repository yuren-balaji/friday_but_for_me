package com.example.first_app_0_0_1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.first_app_0_0_1.data.Automation
import com.example.first_app_0_0_1.data.AutomationDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AutomationViewModel(private val automationDao: AutomationDao) : ViewModel() {

    val automations: StateFlow<List<Automation>?> = automationDao.getAllAutomations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addAutomation(automation: Automation) {
        viewModelScope.launch {
            automationDao.insertAutomation(automation)
        }
    }

    fun updateAutomation(automation: Automation) {
        viewModelScope.launch {
            automationDao.updateAutomation(automation)
        }
    }

    fun deleteAutomation(automation: Automation) {
        viewModelScope.launch {
            automationDao.deleteAutomation(automation)
        }
    }
}

class AutomationViewModelFactory(private val automationDao: AutomationDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AutomationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AutomationViewModel(automationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
