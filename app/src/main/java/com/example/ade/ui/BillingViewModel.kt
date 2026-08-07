package com.example.ade.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ade.data.AppDatabase
import com.example.ade.logic.BillingEngine
import com.example.ade.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BillingViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.billDao()

    var previousIndex by mutableStateOf("")
    var currentIndex by mutableStateOf("")
    var usageType by mutableStateOf(UsageType.CAT_I)
    var wholesaleTvaRate by mutableStateOf(0.19) // Par défaut 19% pour la Cat V
    
    var lastResult by mutableStateOf<CalculationResult?>(null)
    
    var showError by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    private val _history = MutableStateFlow<List<BillRecord>>(emptyList())
    val history: StateFlow<List<BillRecord>> = _history

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = dao.getAllBills()
        }
    }

    fun calculate(): Boolean {
        val prev = previousIndex.toDoubleOrNull()
        val curr = currentIndex.toDoubleOrNull()

        if (prev == null || curr == null) {
            errorMessage = "Veuillez saisir des index valides."
            showError = true
            return false
        }

        if (curr < prev) {
            errorMessage = "Le nouvel index doit être supérieur ou égal à l'ancien."
            showError = true
            return false
        }

        showError = false
        val result = BillingEngine.calculate(usageType, prev, curr, wholesaleTvaRate)
        lastResult = result

        // Sauvegarde dans l'historique
        viewModelScope.launch {
            dao.insertBill(
                BillRecord(
                    usageType = usageType,
                    previousIndex = prev,
                    currentIndex = curr,
                    consumption = result.consumption.toDouble(),
                    totalTTC = result.montantFacture.toDouble()
                )
            )
            loadHistory()
        }
        
        return true
    }

    fun clearInputs() {
        previousIndex = ""
        currentIndex = ""
        showError = false
        lastResult = null
    }

    fun deleteRecord(record: BillRecord) {
        viewModelScope.launch {
            dao.deleteBill(record)
            loadHistory()
        }
    }
}
