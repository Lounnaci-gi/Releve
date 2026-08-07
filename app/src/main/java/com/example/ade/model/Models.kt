package com.example.ade.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UsageType(val label: String, val code: String) {
    CAT_I("Catégorie I - Ménages", "11-19"),
    CAT_II("Catégorie II - Administration", "20-29"),
    CAT_III("Catégorie III - Commercial", "30-39"),
    CAT_IV("Catégorie IV - Industriel", "40-49"),
    CAT_V("Catégorie V - Vente en Gros", "15")
}

@Entity(tableName = "billing_history")
data class BillRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val usageType: UsageType,
    val previousIndex: Double,
    val currentIndex: Double,
    val consumption: Double,
    val fixedAmount: Double,
    val variableAmount: Double,
    val totalTTC: Double,
    val wholesaleTvaRate: Double = 0.0
)

data class CalculationResult(
    val consumption: Double,
    val fixedAmount: Double,
    val variableAmount: Double,
    val totalTTC: Double,
    val tiers: List<TierDetail> = emptyList(),
    val tvaAmount: Double = 0.0,
    val isWholesale: Boolean = false
)

data class TierDetail(
    val label: String,
    val volume: Double,
    val rate: Double,
    val amount: Double
)
