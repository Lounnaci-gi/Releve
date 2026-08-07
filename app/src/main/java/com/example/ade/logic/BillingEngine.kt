package com.example.ade.logic

import com.example.ade.model.*
import kotlin.math.floor
import kotlin.math.round

object BillingEngine {

    /**
     * Calcule la facture avec précision.
     * @param wholesaleTvaRate Utilisé uniquement pour la Catégorie V (0.09 ou 0.19)
     */
    fun calculate(
        usageType: UsageType,
        previousIndex: Double,
        currentIndex: Double,
        wholesaleTvaRate: Double = 0.19
    ): CalculationResult {
        val consumption = if (currentIndex >= previousIndex) {
            currentIndex - previousIndex
        } else {
            0.0 
        }

        return when (usageType) {
            UsageType.CAT_I -> calculateCategoryI(consumption)
            UsageType.CAT_II, UsageType.CAT_III -> calculateCategoryII_III(consumption)
            UsageType.CAT_IV -> calculateCategoryIV(consumption)
            UsageType.CAT_V -> calculateCategoryV(consumption, wholesaleTvaRate)
        }
    }

    private fun calculateCategoryI(consumption: Double): CalculationResult {
        val tiers = mutableListOf<TierDetail>()
        var remaining = consumption
        val config = TariffConfig.CategoryI

        // Tranche 1: 0 à 25 m³
        val v1 = remaining.coerceAtMost(config.LIMIT_T1)
        if (v1 > 0 || consumption == 0.0) {
            val amount = floor(v1 * config.RATE_T1 * 100) / 100.0
            tiers.add(TierDetail("Tranche 1 (0-25)", v1, config.RATE_T1, amount))
        }
        remaining -= v1

        // Tranche 2: 26 à 55 m³
        val v2 = remaining.coerceAtMost(config.LIMIT_T2)
        if (v2 > 0) {
            val amount = floor(v2 * config.RATE_T2 * 100) / 100.0
            tiers.add(TierDetail("Tranche 2 (26-55)", v2, config.RATE_T2, amount))
        }
        remaining -= v2

        // Tranche 3: 56 à 82 m³
        val v3 = remaining.coerceAtMost(config.LIMIT_T3)
        if (v3 > 0) {
            val amount = floor(v3 * config.RATE_T3 * 100) / 100.0
            tiers.add(TierDetail("Tranche 3 (56-82)", v3, config.RATE_T3, amount))
        }
        remaining -= v3

        // Tranche 4: au-delà de 82 m³
        if (remaining > 0) {
            val amount = floor(remaining * config.RATE_T4 * 100) / 100.0
            tiers.add(TierDetail("Tranche 4 (>82)", remaining, config.RATE_T4, amount))
        }

        val variableAmount = tiers.sumOf { it.amount }
        val fixedAmount = config.FIXED_FEE_TTC
        val totalTTC = round((fixedAmount + variableAmount) * 100) / 100.0
        
        return CalculationResult(
            consumption = consumption,
            fixedAmount = fixedAmount,
            variableAmount = variableAmount,
            totalTTC = totalTTC,
            tiers = tiers
        )
    }

    private fun calculateCategoryII_III(consumption: Double): CalculationResult {
        val config = TariffConfig.CategoryII_III
        val variableAmount = consumption * config.VARIABLE_RATE_TTC
        val fixedAmount = config.FIXED_FEE_TTC
        val totalTTC = round((fixedAmount + variableAmount) * 100) / 100.0
        
        val tiers = listOf(
            TierDetail("Consommation forfaitaire", consumption, config.VARIABLE_RATE_TTC, variableAmount)
        )

        return CalculationResult(
            consumption = consumption,
            fixedAmount = fixedAmount,
            variableAmount = variableAmount,
            totalTTC = totalTTC,
            tiers = tiers
        )
    }

    private fun calculateCategoryIV(consumption: Double): CalculationResult {
        val config = TariffConfig.CategoryIV
        val variableAmount = consumption * config.VARIABLE_RATE_TTC
        val fixedAmount = config.FIXED_FEE_TTC
        val totalTTC = round((fixedAmount + variableAmount) * 100) / 100.0
        
        val tiers = listOf(
            TierDetail("Consommation industrielle", consumption, config.VARIABLE_RATE_TTC, variableAmount)
        )

        return CalculationResult(
            consumption = consumption,
            fixedAmount = fixedAmount,
            variableAmount = variableAmount,
            totalTTC = totalTTC,
            tiers = tiers
        )
    }

    private fun calculateCategoryV(consumption: Double, tvaRate: Double): CalculationResult {
        val config = TariffConfig.CategoryV
        val variableHT = consumption * config.VARIABLE_RATE_HT
        val fixedHT = config.FIXED_FEE_HT
        val totalHT = fixedHT + variableHT
        val tvaAmount = totalHT * tvaRate
        val totalTTC = round((totalHT + tvaAmount) * 100) / 100.0

        val tiers = listOf(
            TierDetail("Consommation (HT)", consumption, config.VARIABLE_RATE_HT, variableHT)
        )

        return CalculationResult(
            consumption = consumption,
            fixedAmount = fixedHT,
            variableAmount = variableHT,
            tvaAmount = tvaAmount,
            totalTTC = totalTTC,
            tiers = tiers,
            isWholesale = true
        )
    }
}
