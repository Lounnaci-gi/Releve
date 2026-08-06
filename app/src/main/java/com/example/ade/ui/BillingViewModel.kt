package com.example.ade.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.ade.data.AppDatabase
import com.example.ade.logic.BillingEngine
import com.example.ade.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BillingViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.billDao()

    var usageType by mutableStateOf(UsageType.DOMESTIC)
    var previousIndex by mutableStateOf("")
    var currentIndex by mutableStateOf("")

    var showError by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    private val _calculationResult = MutableStateFlow<CalculationResult?>(null)
    val calculationResult = _calculationResult.asStateFlow()

    private val _history = MutableStateFlow<List<BillRecord>>(emptyList())
    val history = _history.asStateFlow()

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
            errorMessage = "Veuillez saisir des nombres valides"
            showError = true
            return false
        }

        if (curr < prev) {
            errorMessage = "Le nouvel index doit être supérieur à l'ancien"
            showError = true
            return false
        }

        showError = false
        val result = BillingEngine.calculate(
            usageType = usageType,
            previousIndex = prev,
            currentIndex = curr
        )
        _calculationResult.value = result
        return true
    }

    fun clearInputs() {
        previousIndex = ""
        currentIndex = ""
        showError = false
    }

    fun saveBill() {
        val res = _calculationResult.value ?: return
        viewModelScope.launch {
            val record = BillRecord(
                usageType = usageType,
                previousIndex = previousIndex.toDoubleOrNull() ?: 0.0,
                currentIndex = currentIndex.toDoubleOrNull() ?: 0.0,
                consumption = res.consumption,
                waterAmount = res.waterAmount,
                sanitationAmount = res.sanitationAmount,
                fixedFees = res.fixedFees,
                regulationFees = res.regulationFees,
                tvaAmount = res.waterTva + res.sanitationTva,
                totalTTC = res.totalTTC
            )
            dao.insertBill(record)
            loadHistory()
        }
    }

    fun deleteBill(record: BillRecord) {
        viewModelScope.launch {
            dao.deleteBill(record)
            loadHistory()
        }
    }
}
