package com.lifetracker.ui.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifetracker.data.db.HealthLogEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.HealthGreen
import com.lifetracker.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: HealthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showLogDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<HealthLogEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("💪 Health & Fitness") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLogDialog = true },
                containerColor = HealthGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Health Data")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // BMI Card
            item {
                if (state.bmi > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BMI", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    String.format("%.1f", state.bmi),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthGreen
                                )
                                Text(state.bmiCategory, style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Weight", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "${state.user?.weightKg ?: "--"} kg",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Height", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "${state.user?.heightCm ?: "--"} cm",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthGreen
                                )
                            }
                        }
                    }
                }
            }

            // Today's stats
            item {
                SectionHeader(title = "Today's Stats")
            }
            item {
                state.todayLog?.let { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Calories",
                            value = "${log.caloriesConsumed}",
                            subtitle = "kcal",
                            accentColor = HealthGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Steps",
                            value = "${log.stepsCount}",
                            subtitle = "steps",
                            accentColor = HealthGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } ?: EmptyStateView(
                    message = "No health data logged today. Tap + to add.",
                    icon = Icons.Default.FitnessCenter
                )
            }

            // Recent logs
            item {
                SectionHeader(title = "Recent Logs")
            }
            if (state.recentLogs.isEmpty()) {
                item {
                    EmptyStateView(
                        message = "No health logs yet.",
                        icon = Icons.Default.FitnessCenter
                    )
                }
            } else {
                items(state.recentLogs.take(10)) { log ->
                    HealthLogItem(log = log, onDelete = { logToDelete = log })
                }
            }
        }
    }

    if (showLogDialog) {
        HealthLogDialog(
            existingLog = state.todayLog,
            onDismiss = { showLogDialog = false },
            onSave = { weight, calories, protein, carbs, fat, steps, notes ->
                viewModel.saveHealthLog(weight, calories, protein, carbs, fat, steps, notes)
                showLogDialog = false
            }
        )
    }

    logToDelete?.let { log ->
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteLog(log)
                logToDelete = null
            },
            onDismiss = { logToDelete = null }
        )
    }
}

@Composable
fun HealthLogItem(log: HealthLogEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(log.date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "${log.caloriesConsumed} kcal • ${log.weightKg?.let { "${it} kg" } ?: "No weight"} • ${log.stepsCount} steps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun HealthLogDialog(
    existingLog: HealthLogEntity?,
    onDismiss: () -> Unit,
    onSave: (Float?, Int, Float, Float, Float, Int, String) -> Unit
) {
    var weightText by remember { mutableStateOf(existingLog?.weightKg?.toString() ?: "") }
    var caloriesText by remember { mutableStateOf(existingLog?.caloriesConsumed?.toString() ?: "") }
    var proteinText by remember { mutableStateOf(existingLog?.proteinG?.toString() ?: "") }
    var carbsText by remember { mutableStateOf(existingLog?.carbsG?.toString() ?: "") }
    var fatText by remember { mutableStateOf(existingLog?.fatG?.toString() ?: "") }
    var stepsText by remember { mutableStateOf(existingLog?.stepsCount?.toString() ?: "") }
    var notes by remember { mutableStateOf(existingLog?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Health Data") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = weightText, onValueChange = { weightText = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caloriesText, onValueChange = { caloriesText = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = proteinText, onValueChange = { proteinText = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbsText, onValueChange = { carbsText = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = fatText, onValueChange = { fatText = it },
                    label = { Text("Fat (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stepsText, onValueChange = { stepsText = it },
                    label = { Text("Steps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    weightText.toFloatOrNull(),
                    caloriesText.toIntOrNull() ?: 0,
                    proteinText.toFloatOrNull() ?: 0f,
                    carbsText.toFloatOrNull() ?: 0f,
                    fatText.toFloatOrNull() ?: 0f,
                    stepsText.toIntOrNull() ?: 0,
                    notes
                )
            }) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
