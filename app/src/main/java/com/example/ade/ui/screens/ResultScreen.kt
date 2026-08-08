package com.example.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
        // En-tête : Montant Final
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "MONTANT DE LA FACTURE", 
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "${result.montantFacture} DA",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    "Consommation : ${result.consumption} m³",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (result.isSimplified || result.isWholesale) {
            SimplifiedResult(result)
        } else {
            DetailedResult(result)
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("NOUVEAU CALCUL", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailedResult(result: CalculationResult) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Bloc EAU (Bleu clair)
        InvoiceBlock(
            title = "BLOC EAU (1)",
            containerColor = Color(0xFFE3F2FD),
            contentColor = Color(0xFF0D47A1),
            lines = result.waterLines,
            fixedFee = result.fixedFeeWater,
            subTotal = result.subTotalWater
        )

        // Bloc ASSAINISSEMENT (Vert clair)
        InvoiceBlock(
            title = "BLOC ASSAINISSEMENT (2)",
            containerColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF1B5E20),
            lines = result.sanitationLines,
            fixedFee = result.fixedFeeSanitation,
            subTotal = result.subTotalSanitation
        )

        // Bloc TAXES ET REDEVANCES
        TaxesBlock(result)
    }
}

@Composable
fun SimplifiedResult(result: CalculationResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(result.usageType.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider()
            
            ResultRow("Redevances Fixes", "${result.fixedFeeWater} DA")
            ResultRow("Consommation (${result.consumption} m³)", "${result.waterUsageHT} DA")
            
            if (result.isWholesale) {
                ResultRow("TVA", "${result.tvaTotal} DA")
            }
            
            HorizontalDivider(thickness = 2.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TOTAL TTC", fontWeight = FontWeight.Bold)
                Text("${result.montantFacture} DA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun InvoiceBlock(
    title: String,
    containerColor: Color,
    contentColor: Color,
    lines: List<InvoiceLine>,
    fixedFee: BigDecimal,
    subTotal: BigDecimal
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = contentColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Libellé", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Qte", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("P.U", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Montant", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = contentColor.copy(alpha = 0.2f))
            
            // Redevance Fixe
            DataRow("Redevance fixe", "—", "—", fixedFee.toString())
            
            // Tranches
            lines.forEach { line ->
                DataRow(line.label, line.quantity.toString(), line.priceUnit.toString(), line.amount.toString())
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = contentColor.copy(alpha = 0.5f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SOUS-TOTAL", fontWeight = FontWeight.Bold, color = contentColor)
                Text("${subTotal} DA", fontWeight = FontWeight.Bold, color = contentColor)
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
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Libellé", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Assiette", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Taux", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Montant", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            DataRow("TVA", (result.subTotalWater.add(result.subTotalSanitation)).toString(), "9.00 %", result.tvaTotal.toString())
            DataRow("Redevance gestion", result.consumption.toString(), "3.00 DA/m3", result.redevanceGestion.toString())
            DataRow("Redevance qualité", result.waterUsageHT.toString(), "4.00 %", result.redevanceQualiteEau.toString())
            DataRow("Redevance économie", result.waterUsageHT.toString(), "4.00 %", result.redevanceEconomieEau.toString())
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 2.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SOUS-TOTAL TAXES", fontWeight = FontWeight.Bold)
                Text("${result.subTotalTaxes} DA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun DataRow(c1: String, c2: String, c3: String, c4: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(c1, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall)
        Text(c2, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
        Text(c3, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
        Text(c4, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
