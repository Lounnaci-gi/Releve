package com.example.ade.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ade.model.*
import com.example.ade.ui.BillingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(navController: NavController, viewModel: BillingViewModel) {
    val scrollState = rememberScrollState()
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Saisie des Index",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearInputs() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Vider")
                    }
                }
                
                Text(
                    "Calculez précisément la facture ADE selon les barèmes officiels.", 
                    style = MaterialTheme.typography.bodySmall
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Catégorie d'abonné", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = viewModel.usageType.label,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        UsageType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(type.label, fontWeight = FontWeight.Bold)
                                        Text("Codes ${type.code}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    viewModel.usageType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (viewModel.usageType == UsageType.CAT_V) {
                    Text("Taux de TVA (Vente en Gros)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = viewModel.wholesaleTvaRate == 9.0,
                            onClick = { viewModel.wholesaleTvaRate = 9.0 }
                        )
                        Text("9%", modifier = Modifier.padding(end = 16.dp))
                        RadioButton(
                            selected = viewModel.wholesaleTvaRate == 19.0,
                            onClick = { viewModel.wholesaleTvaRate = 19.0 }
                        )
                        Text("19%")
                    }
                }

                AnimatedVisibility(visible = viewModel.showError) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(viewModel.errorMessage, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = viewModel.previousIndex,
                        onValueChange = { viewModel.previousIndex = it },
                        label = { Text("Ancien Index") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = viewModel.currentIndex,
                        onValueChange = { viewModel.currentIndex = it },
                        label = { Text("Nouveau Index") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        Button(
            onClick = {
                if (viewModel.calculate()) {
                    navController.navigate("result")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Calculer la facture", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
