package com.lifetracker.ui.hydration

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
import com.lifetracker.data.db.HydrationLogEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.HydrationBlue
import com.lifetracker.viewmodel.HydrationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(
    viewModel: HydrationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<HydrationLogEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💧 Hydration") },
                actions = {
                    IconButton(onClick = { showGoalDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Set Goal")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = HydrationBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Water Log")
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
            // Progress ring
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProgressRing(
                            progress = state.progressFraction,
                            size = 160.dp,
                            strokeWidth = 16.dp,
                            progressColor = HydrationBlue
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${state.todayTotalMl}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HydrationBlue
                                )
                                Text(
                                    text = "ml",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Daily Goal: ${state.dailyGoalMl} ml",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${(state.progressFraction * 100).toInt()}% completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Quick add buttons
            item {
                SectionHeader(title = "Quick Add")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(150, 250, 350, 500).forEach { amount ->
                        OutlinedButton(
                            onClick = { viewModel.addWaterLog(amount) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${amount}ml", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Today's logs
            item {
                SectionHeader(title = "Today's Logs")
            }

            if (state.todayLogs.isEmpty()) {
                item {
                    EmptyStateView(
                        message = "No water logged today. Stay hydrated! 💧",
                        icon = Icons.Default.WaterDrop
                    )
                }
            } else {
                items(state.todayLogs) { log ->
                    HydrationLogItem(
                        log = log,
                        onDelete = { logToDelete = log }
                    )
                }
            }
        }
    }

    // Add water dialog
    if (showAddDialog) {
        AddWaterDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { amount, note ->
                viewModel.addWaterLog(amount, note)
                showAddDialog = false
            }
        )
    }

    // Set goal dialog
    if (showGoalDialog) {
        SetGoalDialog(
            currentGoal = state.dailyGoalMl,
            currentWeight = state.user?.weightKg ?: 70f,
            onDismiss = { showGoalDialog = false },
            onSetGoal = { goal ->
                viewModel.setCustomGoal(goal)
                showGoalDialog = false
            },
            onSetFromWeight = { weight ->
                viewModel.updateGoalFromWeight(weight)
                showGoalDialog = false
            }
        )
    }

    // Delete confirmation
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
fun HydrationLogItem(
    log: HydrationLogEntity,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = timeFormat.format(Date(log.timestamp))

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = HydrationBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${log.amountMl} ml",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = time + if (log.note.isNotEmpty()) " • ${log.note}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddWaterDialog(
    onDismiss: () -> Unit,
    onAdd: (Int, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Water Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toIntOrNull() ?: 0
                    if (amount > 0) onAdd(amount, note)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SetGoalDialog(
    currentGoal: Int,
    currentWeight: Float,
    onDismiss: () -> Unit,
    onSetGoal: (Int) -> Unit,
    onSetFromWeight: (Float) -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toString()) }
    var weightText by remember { mutableStateOf(currentWeight.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Water Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Custom Goal (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Divider()
                Text(
                    "Or calculate from body weight:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Body Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Formula: weight × 35 ml/kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Column {
                Button(
                    onClick = {
                        val goal = goalText.toIntOrNull() ?: currentGoal
                        onSetGoal(goal)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Set Custom Goal") }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        val weight = weightText.toFloatOrNull() ?: currentWeight
                        onSetFromWeight(weight)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Calculate from Weight") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
