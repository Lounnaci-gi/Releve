package com.example.ade.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ade.ui.BillingViewModel
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("MONTANT TOTAL TTC", style = MaterialTheme.typography.labelLarge)
                Text(
                    String.format(Locale.US, "%.2f DA", result.totalTTC),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                ) {
                    Text(
                        "Consommation : ${String.format(Locale.US, "%.2f", result.consumption)} m³",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text("Détail du calcul", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tiers breakdown
                result.tiers.forEach { tier ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tier.label, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${String.format(Locale.US, "%.2f", tier.volume)} m³ × ${String.format(Locale.US, "%.4f", tier.rate)} DA",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            String.format(Locale.US, "%.2f DA", tier.amount),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }

                // Fixed Amount
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(if (result.isWholesale) "Partie Fixe (HT)" else "Partie Fixe (Abonnement)", modifier = Modifier.weight(1f))
                    Text(String.format(Locale.US, "%.2f DA", result.fixedAmount), fontWeight = FontWeight.Bold)
                }

                if (result.isWholesale) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("TVA", modifier = Modifier.weight(1f))
                        Text(String.format(Locale.US, "%.2f DA", result.tvaAmount), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(if (result.isWholesale) "Total Variable HT" else "Total Variable TTC", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text(String.format(Locale.US, "%.2f DA", result.variableAmount), style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(if (result.isWholesale) "Total Fixe HT" else "Total Fixe TTC", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text(String.format(Locale.US, "%.2f DA", result.fixedAmount), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Nouveau calcul")
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.width(8.dp))
            Text(
                "Calcul précis selon barèmes ADE et loi de finances.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
