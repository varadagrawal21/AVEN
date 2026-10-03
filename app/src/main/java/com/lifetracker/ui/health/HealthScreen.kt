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
import com.lifetracker.ui.theme.*
import androidx.compose.ui.window.DialogProperties
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
            GlassTopAppBar(title = "Health & Fitness")
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showLogDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Log Health Data",
                accentColor = HealthGreen
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.bmi > 0) {
                item {
                    GlassCard(elevation = GlassElevation.Medium) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatColumn("BMI", String.format("%.1f", state.bmi), state.bmiCategory, HealthGreen)
                            StatColumn("Weight", "${state.user?.weightKg ?: "--"}", "kg", HealthGreen)
                            StatColumn("Height", "${state.user?.heightCm ?: "--"}", "cm", HealthGreen)
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Today's Stats")
            }
            item {
                state.todayLog?.let { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardStatCard(
                            title = "Calories",
                            value = "${log.caloriesConsumed}",
                            subtitle = "kcal",
                            icon = Icons.Default.LocalFireDepartment,
                            accentColor = HealthGreen,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "Steps",
                            value = "${log.stepsCount}",
                            subtitle = "steps",
                            icon = Icons.Default.DirectionsWalk,
                            accentColor = HealthGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } ?: EmptyStateView(
                    message = "No health data logged today. Tap + to add.",
                    icon = Icons.Default.FitnessCenter
                )
            }

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
fun StatColumn(label: String, value: String, subtitle: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = WhiteMedium)
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        if (subtitle.isNotEmpty()) {
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = WhiteLow)
        }
    }
}

@Composable
fun HealthLogItem(log: HealthLogEntity, onDelete: () -> Unit) {
    GlassListItem(onClick = null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    log.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WhiteHigh
                )
                Text(
                    "${log.caloriesConsumed} kcal - ${log.weightKg?.let { "${it} kg" } ?: "No weight"} - ${log.stepsCount} steps",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorColor)
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
        containerColor = GlassHeavy,
        title = { Text("Log Health Data", color = WhiteHigh) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GlassTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = "Weight (kg)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                GlassTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it },
                    label = "Calories (kcal)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        label = "Protein (g)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    GlassTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        label = "Carbs (g)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                GlassTextField(
                    value = fatText,
                    onValueChange = { fatText = it },
                    label = "Fat (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                GlassTextField(
                    value = stepsText,
                    onValueChange = { stepsText = it },
                    label = "Steps",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                GlassTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes"
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(onClick = {
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
            GlassOutlineButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = GlassShapes.Large,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}
