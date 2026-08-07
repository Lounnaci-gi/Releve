package com.example.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ade.model.CalculationResult
import com.example.ade.model.InvoiceLine
import com.example.ade.ui.BillingViewModel
import java.math.BigDecimal
import java.util.*

@Composable
fun ResultScreen(navController: NavController, viewModel: BillingViewModel) {
    val result = viewModel.lastResult ?: return
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête Total
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("MONTANT DE LA FACTURE", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium)
                Text(
                    "${result.montantFacture} DA",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    "Consommation totale : ${result.consumption} m³",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Bloc 1 : EAU
        InvoiceBlock(title = "BLOC EAU (1)", lines = result.waterLines, fixedFee = result.fixedFeeWater, subTotal = result.subTotalWater)

        // Bloc 2 : ASSAINISSEMENT
        if (!result.isWholesale) {
            InvoiceBlock(title = "BLOC ASSAINISSEMENT (2)", lines = result.sanitationLines, fixedFee = result.fixedFeeSanitation, subTotal = result.subTotalSanitation)
        }

        // Bloc 3 : TAXES ET REDEVANCES
        TaxesBlock(result)

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("NOUVEAU CALCUL")
        }
    }
}

@Composable
fun InvoiceBlock(title: String, lines: List<InvoiceLine>, fixedFee: BigDecimal, subTotal: BigDecimal) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            HeaderRow("Libellé", "Qte", "P.U", "Montant")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            // Fixed Fee
            DataRow("Redevance fixe", "-", "-", fixedFee.toString())
            
            // Tiers
            lines.forEach { line ->
                DataRow(line.label, line.quantity.toString(), line.priceUnit.toString(), line.amount.toString())
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 2.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SOUS-TOTAL", fontWeight = FontWeight.Bold)
                Text("${subTotal} DA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun TaxesBlock(result: CalculationResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("TAXES ET REDEVANCES (3)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            HeaderRow("Libellé", "Assiette", "Taux", "Montant")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            DataRow("TVA", (result.subTotalWater.add(result.subTotalSanitation)).toString(), "9.00 %", result.tvaTotal.toString())
            if (!result.isWholesale) {
                DataRow("Redevance gestion", result.consumption.toString(), "3.00 DA", result.redevanceGestion.toString())
                DataRow("Redevance qualité", result.waterUsageHT.toString(), "4.00 %", result.redevanceQualiteEau.toString())
                DataRow("Redevance économie", result.waterUsageHT.toString(), "4.00 %", result.redevanceEconomieEau.toString())
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 2.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SOUS-TOTAL TAXES", fontWeight = FontWeight.Bold)
                Text("${result.subTotalTaxes} DA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun HeaderRow(c1: String, c2: String, c3: String, c4: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(c1, modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(c2, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text(c3, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text(c4, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
fun DataRow(label: String, qte: String, pu: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall)
        Text(qte, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
        Text(pu, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
        Text(amount, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}
