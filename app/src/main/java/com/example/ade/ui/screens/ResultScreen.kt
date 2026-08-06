package com.example.ade.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ade.logic.PdfExporter
import com.example.ade.ui.BillingViewModel
import java.util.Locale

@Composable
fun ResultScreen(navController: NavController, viewModel: BillingViewModel) {
    val result = viewModel.calculationResult.collectAsState().value ?: return
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "Total à payer (TTC)", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    formatAmount(result.totalTTC),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

            Spacer(Modifier.height(8.dp))
            
            ResultRow("Consommation", "${String.format(Locale.getDefault(), "%.2f", result.consumption)} m³")
            HorizontalDivider()
            ResultRow("Montant Eau", formatAmount(result.waterAmount))
            ResultRow("Montant Assainissement", formatAmount(result.sanitationAmount))
            ResultRow("Redevances Fixes", formatAmount(result.fixedFees))
            ResultRow("Frais Régulation", formatAmount(result.regulationFees))
            ResultRow("TVA (Totale)", formatAmount(result.waterTva + result.sanitationTva))

            if (result.tranches.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Détail par tranches", style = MaterialTheme.typography.titleMedium)
                result.tranches.forEach { tranche ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(tranche.range, fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Volume: ${tranche.volume} m³")
                                Text("Eau: ${formatAmount(tranche.waterAmount)}")
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.saveBill()
                    navController.navigate("history") {
                        popUpTo("input") { inclusive = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Enregistrer dans l'historique", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    PdfExporter.exportToPdf(context, result)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Exporter en PDF")
            }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

fun formatAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%.2f DA", amount)
}
