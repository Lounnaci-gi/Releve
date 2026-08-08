package com.example.ade

import com.example.ade.logic.BillingEngine
import com.example.ade.model.UsageType
import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal

class ExampleUnitTest {
    
    @Test
    fun testCategoryI_17m3() {
        val res = BillingEngine.calculate(UsageType.CAT_I, 0.0, 17.0)
        // Tests demandés : sousTotalEau=347.10, sousTotalAss=99.95, tvaEau=31.24, tvaAss=9.00,
        // redevanceGestion=51.00, rqe=4.28, ree=4.28, montantFacture=546.85
        assertEquals(BigDecimal("347.10"), res.subTotalWater)
        assertEquals(BigDecimal("99.95"), res.subTotalSanitation)
        assertEquals(BigDecimal("31.24"), res.tvaEau)
        assertEquals(BigDecimal("9.00"), res.tvaSanitation)
        assertEquals(BigDecimal("51.00"), res.redevanceGestion)
        assertEquals(BigDecimal("4.28"), res.redevanceQualiteEau)
        assertEquals(BigDecimal("4.28"), res.redevanceEconomieEau)
        assertEquals(BigDecimal("546.85"), res.montantFacture)
    }

    @Test
    fun testCategoryI_25m3() {
        val res = BillingEngine.calculate(UsageType.CAT_I, 0.0, 25.0)
        assertEquals(BigDecimal("650.32"), res.montantFacture)
    }

    @Test
    fun testCategoryI_10m3() {
        val res = BillingEngine.calculate(UsageType.CAT_I, 0.0, 10.0)
        assertEquals(BigDecimal("456.33"), res.montantFacture)
    }

    @Test
    fun testCategoryI_91m3() {
        val res = BillingEngine.calculate(UsageType.CAT_I, 0.0, 91.0)
        // Tolérance ±0.01 DA autorisée par le prompt
        val diff = res.montantFacture.subtract(BigDecimal("3873.22")).abs()
        assertTrue("Différence trop grande : $diff", diff <= BigDecimal("0.01"))
    }

    @Test
    fun testCategoryI_120m3() {
        val res = BillingEngine.calculate(UsageType.CAT_I, 0.0, 120.0)
        val diff = res.montantFacture.subtract(BigDecimal("5832.65")).abs()
        assertTrue("Différence trop grande : $diff", diff <= BigDecimal("0.01"))
    }

    @Test
    fun testCategoryIV_Industriel_678m3() {
        val res = BillingEngine.calculate(UsageType.CAT_IV, 0.0, 678.0)
        // Test demandé : 678 m³ → 52240.61 DA exact à 0.01 DA près
        val diff = res.montantFacture.subtract(BigDecimal("52240.61")).abs()
        assertTrue("Différence trop grande : $diff", diff <= BigDecimal("0.01"))
    }

    @Test
    fun testCategoryV_Wholesale_124890m3() {
        val res = BillingEngine.calculate(UsageType.CAT_V, 0.0, 124890.0, 19.0)
        // Test : 124890 m³ à TVA 19% → 2945916.16 DA
        assertEquals(BigDecimal("2945916.16"), res.montantFacture)
    }

    @Test
    fun testCategoryV_Wholesale_116710m3() {
        val res = BillingEngine.calculate(UsageType.CAT_V, 0.0, 116710.0, 9.0)
        // Test : 116710 m³ à TVA 9% → 2521641.10 DA
        assertEquals(BigDecimal("2521641.10"), res.montantFacture)
    }
}
