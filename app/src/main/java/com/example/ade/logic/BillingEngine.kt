package com.example.ade.logic

import com.example.ade.model.*

object BillingEngine {

    fun calculate(
        usageType: UsageType,
        previousIndex: Double,
        currentIndex: Double
    ): CalculationResult {
        val qte = (currentIndex - previousIndex).coerceAtLeast(0.0)
        
        return when (usageType) {
            UsageType.DOMESTIC -> calculateDomestic(qte)
            UsageType.COMMERCIAL, UsageType.PROFESSIONAL -> calculateProfessional(qte, usageType)
        }
    }

    private fun calculateDomestic(qte: Double): CalculationResult {
        val tranches = mutableListOf<TrancheDetail>()
        var remaining = qte
        
        // Tranche 1: 0-25
        val v1 = remaining.coerceAtMost(25.0)
        tranches.add(TrancheDetail("0 - 25 m³", v1, 6.30, 2.35, v1 * 6.30, v1 * 2.35))
        remaining -= v1
        
        // Tranche 2: 26-55 (30m³)
        val v2 = remaining.coerceAtMost(30.0)
        tranches.add(TrancheDetail("26 - 55 m³", v2, 20.48, 7.64, v2 * 20.48, v2 * 7.64))
        remaining -= v2
        
        // Tranche 3: 56-82 (27m³)
        val v3 = remaining.coerceAtMost(27.0)
        tranches.add(TrancheDetail("56 - 82 m³", v3, 34.65, 12.93, v3 * 34.65, v3 * 12.93))
        remaining -= v3
        
        // Tranche 4: > 82
        if (remaining > 0) {
            tranches.add(TrancheDetail("> 82 m³", remaining, 40.95, 15.28, remaining * 40.95, remaining * 15.28))
        }

        val waterSum = tranches.sumOf { it.waterAmount }
        val sanitationSum = tranches.sumOf { it.sanitationAmount }
        
        val fixedFees = 60.0 + 450.0 + 19.0 // RFASS + RFA + TVASS
        val regulationFees = 4.0 + 4.0 + 3.0 // RQE + REE + RDG
        
        val waterTva = waterSum * 0.09
        val sanTva = sanitationSum * 0.09
        
        val totalTTC = waterSum + sanitationSum + fixedFees + regulationFees + waterTva + sanTva
        
        return CalculationResult(
            consumption = qte,
            waterAmount = waterSum,
            sanitationAmount = sanitationSum,
            waterTva = waterTva,
            sanitationTva = sanTva,
            fixedFees = fixedFees,
            regulationFees = regulationFees,
            totalTTC = totalTTC,
            tranches = tranches
        )
    }

    private fun calculateProfessional(qte: Double, usageType: UsageType): CalculationResult {
        val (waterRate, sanRate) = when (usageType) {
            UsageType.COMMERCIAL -> 34.65 to 12.93
            else -> 40.95 to 15.28
        }
        
        val waterAmount = qte * waterRate
        val sanAmount = qte * sanRate
        
        val fixedFees = 700.0 + 1500.0 // RFASS + RFA
        val regulationFees = 4.0 + 4.0 + 3.0 // RQE + REE + RDG
        
        val waterTva = waterAmount * 0.19 
        val sanTva = sanAmount * 0.19
        
        val totalTTC = waterAmount + sanAmount + fixedFees + regulationFees + waterTva + sanTva
        
        return CalculationResult(
            consumption = qte,
            waterAmount = waterAmount,
            sanitationAmount = sanAmount,
            waterTva = waterTva,
            sanitationTva = sanTva,
            fixedFees = fixedFees,
            regulationFees = regulationFees,
            totalTTC = totalTTC
        )
    }
}
