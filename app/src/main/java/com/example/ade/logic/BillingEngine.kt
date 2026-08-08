package com.example.ade.logic

import com.example.ade.model.*
import java.math.BigDecimal
import java.math.RoundingMode

object BillingEngine {

    fun calculate(
        usageType: UsageType,
        previousIndex: Double,
        currentIndex: Double,
        wholesaleTvaRate: Double = 19.0
    ): CalculationResult {
        val consumptionDouble = (currentIndex - previousIndex).coerceAtLeast(0.0)
        val consumption = BigDecimal.valueOf(consumptionDouble).setScale(2, RoundingMode.HALF_UP)

        return when (usageType) {
            UsageType.CAT_I -> calculateCategoryI(consumption)
            UsageType.CAT_II, UsageType.CAT_III -> calculateSimplified(usageType, consumption, TariffConfig.CategoryII_III.FIXED_PART, TariffConfig.CategoryII_III.VARIABLE_RATE)
            UsageType.CAT_IV -> calculateSimplified(usageType, consumption, TariffConfig.CategoryIV.FIXED_PART, TariffConfig.CategoryIV.VARIABLE_RATE)
            UsageType.CAT_V -> calculateWholesale(consumption, BigDecimal.valueOf(wholesaleTvaRate))
        }
    }

    private fun calculateCategoryI(totalQte: BigDecimal): CalculationResult {
        val config = TariffConfig.CategoryI
        
        // Répartition en tranches
        val tranches = listOf(
            TrancheSpec("Tranche 1 (0-25 m³)", config.T1_CAPACITY, config.WATER_P1, config.SANITATION_P1),
            TrancheSpec("Tranche 2 (26-55 m³)", config.T2_CAPACITY, config.WATER_P2, config.SANITATION_P2),
            TrancheSpec("Tranche 3 (56-82 m³)", config.T3_CAPACITY, config.WATER_P3, config.SANITATION_P3),
            TrancheSpec("Tranche 4 (> 82 m³)", config.T4_CAPACITY, config.WATER_P4, config.SANITATION_P4)
        )

        val waterLines = mutableListOf<InvoiceLine>()
        val sanitationLines = mutableListOf<InvoiceLine>()
        var reste = totalQte

        for (spec in tranches) {
            if (reste <= BigDecimal.ZERO && totalQte > BigDecimal.ZERO) break
            
            val qte = if (spec.capacity == BigDecimal("1000000")) reste else reste.min(spec.capacity)
            if (qte > BigDecimal.ZERO || (totalQte == BigDecimal.ZERO && spec == tranches[0])) {
                val amtWater = qte.multiply(spec.priceEau).setScale(2, RoundingMode.HALF_UP)
                val amtAss = qte.multiply(spec.priceAss).setScale(2, RoundingMode.HALF_UP)
                
                waterLines.add(InvoiceLine(spec.label, spec.priceEau, qte, amtWater))
                sanitationLines.add(InvoiceLine(spec.label, spec.priceAss, qte, amtAss))
                
                reste = reste.subtract(qte)
            }
        }

        // Étape 1 : Somme des lignes arrondies
        val eauUsageHT = waterLines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.amount) }
        val assUsageHT = sanitationLines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.amount) }

        // Étape 2 : Sous-totaux avec RFA
        val subTotalEau = eauUsageHT.add(config.RFA_WATER).setScale(2, RoundingMode.HALF_UP)
        val subTotalAss = assUsageHT.add(config.RFA_SANITATION).setScale(2, RoundingMode.HALF_UP)

        // Étape 3 : TVA séparée
        val tvaEau = subTotalEau.multiply(config.TAX_TVA_RATE).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val tvaAss = subTotalAss.multiply(config.TAX_TVA_RATE).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val tvaTotal = tvaEau.add(tvaAss)

        // Étape 4 : Redevances
        val redevanceGestion = totalQte.multiply(config.TAX_GESTION_RATE).setScale(2, RoundingMode.HALF_UP)
        val redevanceQualite = eauUsageHT.multiply(config.TAX_RQE_RATE).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val redevanceEconomie = eauUsageHT.multiply(config.TAX_REE_RATE).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)

        // Étape 5 : Totaux finaux
        val subTotalTaxes = tvaTotal.add(redevanceGestion).add(redevanceQualite).add(redevanceEconomie)
        val montantFacture = subTotalEau.add(subTotalAss).add(subTotalTaxes)

        return CalculationResult(
            consumption = totalQte,
            usageType = UsageType.CAT_I,
            waterLines = waterLines,
            waterUsageHT = eauUsageHT,
            fixedFeeWater = config.RFA_WATER,
            subTotalWater = subTotalEau,
            sanitationLines = sanitationLines,
            sanitationUsageHT = assUsageHT,
            fixedFeeSanitation = config.RFA_SANITATION,
            subTotalSanitation = subTotalAss,
            tvaEau = tvaEau,
            tvaSanitation = tvaAss,
            tvaTotal = tvaTotal,
            redevanceGestion = redevanceGestion,
            redevanceQualiteEau = redevanceQualite,
            redevanceEconomieEau = redevanceEconomie,
            subTotalTaxes = subTotalTaxes,
            montantFacture = montantFacture
        )
    }

    private fun calculateSimplified(type: UsageType, qte: BigDecimal, fixed: BigDecimal, rate: BigDecimal): CalculationResult {
        val variablePart = qte.multiply(rate).setScale(2, RoundingMode.HALF_UP)
        val total = fixed.add(variablePart).setScale(2, RoundingMode.HALF_UP)

        return CalculationResult(
            consumption = qte,
            usageType = type,
            waterLines = emptyList(),
            waterUsageHT = variablePart,
            fixedFeeWater = fixed,
            subTotalWater = total,
            sanitationLines = emptyList(),
            sanitationUsageHT = BigDecimal.ZERO,
            fixedFeeSanitation = BigDecimal.ZERO,
            subTotalSanitation = BigDecimal.ZERO,
            tvaTotal = BigDecimal.ZERO, // Inclus dans le tarif tout compris pour ces catégories
            subTotalTaxes = BigDecimal.ZERO,
            montantFacture = total,
            isSimplified = true
        )
    }

    private fun calculateWholesale(qte: BigDecimal, tvaRate: BigDecimal): CalculationResult {
        val config = TariffConfig.CategoryV
        val variableHT = qte.multiply(config.PRICE_UNIT_WATER_HT).setScale(2, RoundingMode.HALF_UP)
        val totalHT = config.RFA.add(variableHT)
        val tva = totalHT.multiply(tvaRate).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val totalTTC = totalHT.add(tva)

        return CalculationResult(
            consumption = qte,
            usageType = UsageType.CAT_V,
            waterLines = emptyList(),
            waterUsageHT = variableHT,
            fixedFeeWater = config.RFA,
            subTotalWater = totalHT,
            sanitationLines = emptyList(),
            sanitationUsageHT = BigDecimal.ZERO,
            fixedFeeSanitation = BigDecimal.ZERO,
            subTotalSanitation = BigDecimal.ZERO,
            tvaTotal = tva,
            subTotalTaxes = tva,
            montantFacture = totalTTC,
            isWholesale = true
        )
    }

    private data class TrancheSpec(
        val label: String,
        val capacity: BigDecimal,
        val priceEau: BigDecimal,
        val priceAss: BigDecimal
    )
}
