package com.example.ade.logic

import com.example.ade.model.*
import java.math.BigDecimal
import java.math.RoundingMode

object BillingEngine {

    fun calculate(
        usageType: UsageType,
        previousIndex: Double,
        currentIndex: Double,
        wholesaleTvaRate: Double = 0.19
    ): CalculationResult {
        val consumptionDouble = (currentIndex - previousIndex).coerceAtLeast(0.0)
        val consumption = BigDecimal.valueOf(consumptionDouble).setScale(2, RoundingMode.HALF_UP)

        return when (usageType) {
            UsageType.CAT_I -> calculateCategoryI(consumption)
            else -> calculateWholesale(consumption, BigDecimal.valueOf(wholesaleTvaRate))
        }
    }

    private fun calculateCategoryI(totalQte: BigDecimal): CalculationResult {
        val config = TariffConfig.CategoryI
        val waterLines = mutableListOf<InvoiceLine>()
        val sanitationLines = mutableListOf<InvoiceLine>()
        
        var remaining = totalQte
        
        val tiers = listOf(
            TierConfig("Tranche 1 (0-25 m³)", config.TIER_1_LIMIT, config.WATER_P1, config.SANITATION_P1),
            TierConfig("Tranche 2 (26-55 m³)", config.TIER_2_LIMIT, config.WATER_P2, config.SANITATION_P2),
            TierConfig("Tranche 3 (56-82 m³)", config.TIER_3_LIMIT, config.WATER_P3, config.SANITATION_P3),
            TierConfig("Tranche 4 (> 82 m³)", BigDecimal("1000000"), config.WATER_P4, config.SANITATION_P4)
        )

        for (tier in tiers) {
            if (remaining <= BigDecimal.ZERO && totalQte > BigDecimal.ZERO) break
            
            val qte = if (tier.limit == BigDecimal("1000000")) remaining else remaining.min(tier.limit)
            if (qte > BigDecimal.ZERO || (totalQte == BigDecimal.ZERO && tier == tiers[0])) {
                val effectiveQte = qte.coerceAtLeast(BigDecimal.ZERO)
                
                // Étape 1: Arrondi par ligne
                val waterAmount = effectiveQte.multiply(tier.waterPrice).setScale(2, RoundingMode.HALF_UP)
                val sanitationAmount = effectiveQte.multiply(tier.sanitationPrice).setScale(2, RoundingMode.HALF_UP)
                
                waterLines.add(InvoiceLine(tier.label, tier.waterPrice, effectiveQte, waterAmount))
                sanitationLines.add(InvoiceLine(tier.label, tier.sanitationPrice, effectiveQte, sanitationAmount))
                
                remaining = remaining.subtract(effectiveQte)
            }
        }

        // Étape 1 Bis: Somme des lignes déjà arrondies
        val waterUsageHT = waterLines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.amount) }
        val sanitationUsageHT = sanitationLines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.amount) }

        // Étape 2: Sous-totaux avec redevances fixes
        val subTotalWater = waterUsageHT.add(config.FIXED_FEE_WATER).setScale(2, RoundingMode.HALF_UP)
        val subTotalSanitation = sanitationUsageHT.add(config.FIXED_FEE_SANITATION).setScale(2, RoundingMode.HALF_UP)

        // Étape 3: TVA calculée SÉPARÉMENT
        val tvaEau = subTotalWater.multiply(config.RATE_TVA).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val tvaSanitation = subTotalSanitation.multiply(config.RATE_TVA).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val tvaTotal = tvaEau.add(tvaSanitation)

        // Étape 4: Redevances additionnelles
        val redevanceGestion = totalQte.multiply(config.RATE_REDEVANCE_GESTION).setScale(2, RoundingMode.HALF_UP)
        val redevanceQualiteEau = waterUsageHT.multiply(config.RATE_RQE).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val redevanceEconomieEau = waterUsageHT.multiply(config.RATE_REE).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)

        // Étape 5: Totaux finaux
        val subTotalTaxes = tvaTotal.add(redevanceGestion).add(redevanceQualiteEau).add(redevanceEconomieEau)
        val montantFacture = subTotalWater.add(subTotalSanitation).add(subTotalTaxes)

        return CalculationResult(
            consumption = totalQte,
            waterLines = waterLines,
            waterUsageHT = waterUsageHT,
            fixedFeeWater = config.FIXED_FEE_WATER,
            subTotalWater = subTotalWater,
            sanitationLines = sanitationLines,
            sanitationUsageHT = sanitationUsageHT,
            fixedFeeSanitation = config.FIXED_FEE_SANITATION,
            subTotalSanitation = subTotalSanitation,
            tvaEau = tvaEau,
            tvaSanitation = tvaSanitation,
            tvaTotal = tvaTotal,
            redevanceGestion = redevanceGestion,
            redevanceQualiteEau = redevanceQualiteEau,
            redevanceEconomieEau = redevanceEconomieEau,
            subTotalTaxes = subTotalTaxes,
            montantFacture = montantFacture
        )
    }

    private fun calculateWholesale(totalQte: BigDecimal, tvaRate: BigDecimal): CalculationResult {
        val config = TariffConfig.Wholesale
        val variableHT = totalQte.multiply(config.VARIABLE_RATE_HT).setScale(2, RoundingMode.HALF_UP)
        val totalHT = config.FIXED_FEE_HT.add(variableHT)
        val tvaTotal = totalHT.multiply(tvaRate).setScale(2, RoundingMode.HALF_UP)
        val montantFacture = totalHT.add(tvaTotal)

        return CalculationResult(
            consumption = totalQte,
            waterLines = listOf(InvoiceLine("Vente en Gros", config.VARIABLE_RATE_HT, totalQte, variableHT)),
            waterUsageHT = variableHT,
            fixedFeeWater = config.FIXED_FEE_HT,
            subTotalWater = totalHT,
            sanitationLines = emptyList(),
            sanitationUsageHT = BigDecimal.ZERO,
            fixedFeeSanitation = BigDecimal.ZERO,
            subTotalSanitation = BigDecimal.ZERO,
            tvaEau = tvaTotal,
            tvaSanitation = BigDecimal.ZERO,
            tvaTotal = tvaTotal,
            redevanceGestion = BigDecimal.ZERO,
            redevanceQualiteEau = BigDecimal.ZERO,
            redevanceEconomieEau = BigDecimal.ZERO,
            subTotalTaxes = tvaTotal,
            montantFacture = montantFacture,
            isWholesale = true
        )
    }

    private data class TierConfig(
        val label: String,
        val limit: BigDecimal,
        val waterPrice: BigDecimal,
        val sanitationPrice: BigDecimal
    )
}
