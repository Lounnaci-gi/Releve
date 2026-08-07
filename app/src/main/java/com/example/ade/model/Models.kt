package com.example.ade.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

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
    val totalTTC: Double
)

data class CalculationResult(
    val consumption: BigDecimal,
    
    // Bloc EAU
    val waterLines: List<InvoiceLine>,
    val waterUsageHT: BigDecimal,
    val fixedFeeWater: BigDecimal,
    val subTotalWater: BigDecimal, // (1)
    
    // Bloc ASSAINISSEMENT
    val sanitationLines: List<InvoiceLine>,
    val sanitationUsageHT: BigDecimal,
    val fixedFeeSanitation: BigDecimal,
    val subTotalSanitation: BigDecimal, // (2)
    
    // Bloc TAXES ET REDEVANCES
    val tvaEau: BigDecimal,
    val tvaSanitation: BigDecimal,
    val tvaTotal: BigDecimal,
    val redevanceGestion: BigDecimal,
    val redevanceQualiteEau: BigDecimal,
    val redevanceEconomieEau: BigDecimal,
    val subTotalTaxes: BigDecimal, // (3)
    
    val montantFacture: BigDecimal,
    val isWholesale: Boolean = false
)

data class InvoiceLine(
    val label: String,
    val priceUnit: BigDecimal,
    val quantity: BigDecimal,
    val amount: BigDecimal
)
