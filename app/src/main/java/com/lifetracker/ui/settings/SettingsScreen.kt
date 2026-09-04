package com.lifetracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.lifetracker.data.db.NotificationPrefEntity
import com.lifetracker.ui.components.SectionHeader
import com.lifetracker.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("⚙️ Settings") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile section
            item {
                SectionHeader(title = "Body Measurements & Goals")
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        state.user?.let { user ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Name: ${user.name.ifEmpty { "Not set" }}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Weight: ${user.weightKg} kg", style = MaterialTheme.typography.bodyMedium)
                                    Text("Height: ${user.heightCm} cm", style = MaterialTheme.typography.bodyMedium)
                                    Text("Age: ${user.ageYears} years", style = MaterialTheme.typography.bodyMedium)
                                    Text("Water Goal: ${user.dailyWaterGoalMl} ml/day", style = MaterialTheme.typography.bodyMedium)
                                    Text("Calorie Goal: ${user.dailyCalorieGoal} kcal/day", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        } ?: Text("No profile set up yet.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showProfileDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Edit Profile & Goals")
                        }
                    }
                }
            }

            // Notification preferences
            item {
                SectionHeader(title = "Notification Reminders")
            }

            val modules = listOf(
                "HYDRATION" to "💧 Hydration Reminders",
                "STUDY" to "🎓 Study Reminders",
                "FINANCE" to "💰 Finance Reminders",
                "GENERAL" to "🔔 General Reminders"
            )

            modules.forEach { (moduleKey, moduleLabel) ->
                item {
                    val pref = state.notificationPrefs.find { it.module == moduleKey }
                    NotificationPrefCard(
                        moduleLabel = moduleLabel,
                        pref = pref,
                        onSave = { isEnabled, intervalHours, hour, minute ->
                            viewModel.saveNotificationPref(moduleKey, isEnabled, intervalHours, hour, minute)
                        }
                    )
                }
            }
        }
    }

    if (showProfileDialog) {
        ProfileDialog(
            user = state.user,
            onDismiss = { showProfileDialog = false },
            onSave = { name, weight, height, age, calorieGoal ->
                viewModel.saveUserProfile(name, weight, height, age, calorieGoal)
                showProfileDialog = false
            }
        )
    }
}

@Composable
fun NotificationPrefCard(
    moduleLabel: String,
    pref: NotificationPrefEntity?,
    onSave: (Boolean, Int, Int, Int) -> Unit
) {
    var isEnabled by remember(pref) { mutableStateOf(pref?.isEnabled ?: false) }
    var intervalHours by remember(pref) { mutableStateOf(pref?.intervalHours ?: 2) }
    var reminderHour by remember(pref) { mutableStateOf(pref?.reminderHour ?: 8) }
    var reminderMinute by remember(pref) { mutableStateOf(pref?.reminderMinute ?: 0) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(moduleLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        isEnabled = it
                        onSave(it, intervalHours, reminderHour, reminderMinute)
                    }
                )
            }
            if (isEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Interval: every $intervalHours hours", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = intervalHours.toFloat(),
                    onValueChange = { intervalHours = it.toInt() },
                    valueRange = 1f..12f,
                    steps = 10,
                    onValueChangeFinished = { onSave(isEnabled, intervalHours, reminderHour, reminderMinute) }
                )
                Text(
                    "Reminder time: ${String.format("%02d:%02d", reminderHour, reminderMinute)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ProfileDialog(
    user: com.lifetracker.data.db.UserEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Float, Float, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var weightText by remember { mutableStateOf(user?.weightKg?.toString() ?: "70") }
    var heightText by remember { mutableStateOf(user?.heightCm?.toString() ?: "170") }
    var ageText by remember { mutableStateOf(user?.ageYears?.toString() ?: "25") }
    var calorieGoalText by remember { mutableStateOf(user?.dailyCalorieGoal?.toString() ?: "2000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile & Goals") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = weightText, onValueChange = { weightText = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = heightText, onValueChange = { heightText = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ageText, onValueChange = { ageText = it },
                    label = { Text("Age (years)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = calorieGoalText, onValueChange = { calorieGoalText = it },
                    label = { Text("Daily Calorie Goal (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Water goal will be auto-calculated: weight × 35 ml",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    name,
                    weightText.toFloatOrNull() ?: 70f,
                    heightText.toFloatOrNull() ?: 170f,
                    ageText.toIntOrNull() ?: 25,
                    calorieGoalText.toIntOrNull() ?: 2000
                )
            }) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
