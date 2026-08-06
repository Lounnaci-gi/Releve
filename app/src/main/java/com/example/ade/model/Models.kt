package com.example.ade.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UsageType(val label: String, val code: String) {
    DOMESTIC("Ménage / Domestique", "10"),
    COMMERCIAL("Commerce / Admin / Service", "30"),
    PROFESSIONAL("Professionnel / Industriel", "40")
}

@Entity(tableName = "billing_history")
data class BillRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val usageType: UsageType,
    val previousIndex: Double,
    val currentIndex: Double,
    val consumption: Double,
    
    // Results
    val waterAmount: Double,
    val sanitationAmount: Double,
    val fixedFees: Double,
    val regulationFees: Double,
    val tvaAmount: Double,
    val totalTTC: Double
)

data class CalculationResult(
    val consumption: Double,
    val waterAmount: Double,
    val sanitationAmount: Double,
    val waterTva: Double,
    val sanitationTva: Double,
    val fixedFees: Double,
    val regulationFees: Double,
    val totalTTC: Double,
    val tranches: List<TrancheDetail> = emptyList()
)

data class TrancheDetail(
    val range: String,
    val volume: Double,
    val waterRate: Double,
    val sanitationRate: Double,
    val waterAmount: Double,
    val sanitationAmount: Double
)
