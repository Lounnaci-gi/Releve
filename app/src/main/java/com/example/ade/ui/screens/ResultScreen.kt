package com.example.ade.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Total Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "TOTAL À PAYER", 
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    letterSpacing = 1.5.sp
                )
                Text(
                    formatAmount(result.totalTTC),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Consommation:", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
                    Text("${String.format(Locale.getDefault(), "%.2f", result.consumption)} m³", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Details Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Détail de la simulation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                ResultRow("Montant Eau", formatAmount(result.waterAmount))
                ResultRow("Montant Assainissement", formatAmount(result.sanitationAmount))
                ResultRow("Redevances Fixes", formatAmount(result.fixedFees))
                ResultRow("Frais Régulation", formatAmount(result.regulationFees))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TVA (Totale)", fontWeight = FontWeight.Medium)
                        Text(formatAmount(result.waterTva + result.sanitationTva), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (result.tranches.isNotEmpty()) {
            Text("Répartition par tranches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                ConsumptionChart(result.tranches)
            }

            Spacer(Modifier.height(8.dp))
            result.tranches.forEach { tranche ->
                TrancheItem(tranche)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Action Buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    viewModel.saveBill()
                    navController.navigate("history") {
                        popUpTo("input") { inclusive = false }
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Enregistrer")
            }

            OutlinedButton(
                onClick = { PdfExporter.exportToPdf(context, result) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("PDF")
            }
        }
    }
}

@Composable
fun TrancheItem(tranche: com.example.ade.model.TrancheDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(tranche.range, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("${tranche.volume} m³", fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Part Eau", style = MaterialTheme.typography.bodySmall)
                Text(formatAmount(tranche.waterAmount), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ConsumptionChart(tranches: List<com.example.ade.model.TrancheDetail>) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    // Animate scales outside Canvas
    val scales = tranches.indices.map { index ->
        animateFloatAsState(
            targetValue = if (selectedIndex == -1 || selectedIndex == index) 1f else 0.4f,
            label = "barScale_$index"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(tranches) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val barWidth = width / (tranches.size * 2 - 1)
                        val index = (offset.x / (barWidth * 2)).toInt()
                        if (index in tranches.indices) {
                            selectedIndex = if (selectedIndex == index) -1 else index
                        } else {
                            selectedIndex = -1
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height - 40.dp.toPx() 
            val barWidth = width / (tranches.size * 2 - 1)
            val maxVolume = tranches.maxOfOrNull { it.volume } ?: 1.0
            
            tranches.forEachIndexed { index, tranche ->
                val barHeight = (tranche.volume / maxVolume) * height * scales[index].value
                val x = index * barWidth * 2
                val y = size.height - barHeight.toFloat()
                
                // Draw bar
                drawRoundRect(
                    color = if (index % 2 == 0) primaryColor else secondaryColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight.toFloat()),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    alpha = if (selectedIndex == -1 || selectedIndex == index) 1f else 0.3f
                )

                // Draw quantity text
                if (selectedIndex == index || selectedIndex == -1) {
                    drawContext.canvas.nativeCanvas.apply {
                        val text = "${tranche.volume}"
                        val paint = android.graphics.Paint().apply {
                            color = onSurface.toArgb()
                            textSize = 12.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        drawText(
                            text,
                            x + barWidth / 2,
                            y - 10.dp.toPx(),
                            paint
                        )
                    }
                }
            }
            
            // Baseline
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, size.height),
                end = Offset(width, size.height),
                strokeWidth = 2f
            )
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
