package com.lifetracker.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifetracker.data.db.NotificationPrefEntity
import com.lifetracker.notifications.NotificationHelper
import com.lifetracker.notifications.ReminderScheduler
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.*
import androidx.compose.ui.window.DialogProperties
import com.lifetracker.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val exactAlarmsAvailable = ReminderScheduler.canScheduleExactAlarms(context)
    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    var showProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GlassTopAppBar(title = "Settings")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(title = "Profile & Goals")
            }
            item {
                GlassCard(elevation = GlassElevation.Medium) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        state.user?.let { user ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                SettingRow("Name", user.name.ifEmpty { "Not set" })
                                SettingRow("Weight", "${user.weightKg} kg")
                                SettingRow("Height", "${user.heightCm} cm")
                                SettingRow("Age", "${user.ageYears} years")
                                SettingRow("Water Goal", "${user.dailyWaterGoalMl} ml/day")
                                SettingRow("Calorie Goal", "${user.dailyCalorieGoal} kcal/day")
                            }
                        } ?: Text("No profile set up yet.", style = MaterialTheme.typography.bodyMedium, color = WhiteMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        GlassPrimaryButton(onClick = { showProfileDialog = true }) {
                            Text("Edit Profile & Goals")
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Notification Reminders")
            }
            item {
                NotificationStatusCard(
                    notificationsEnabled = notificationsEnabled,
                    exactAlarmsAvailable = exactAlarmsAvailable,
                    onOpenExactAlarmSettings = { ReminderScheduler.openExactAlarmSettings(context) },
                    onTestNotification = { NotificationHelper.showTestNotification(context) }
                )
            }

            val modules = listOf(
                "HYDRATION" to "Hydration Reminders",
                "STUDY" to "Study Reminders",
                "FINANCE" to "Finance Reminders",
                "GENERAL" to "General Reminders"
            )

            modules.forEach { (moduleKey, moduleLabel) ->
                item {
                    val pref = state.notificationPrefs.find { it.module == moduleKey }
                    NotificationPrefCard(
                        moduleLabel = moduleLabel,
                        pref = pref,
                        moduleKey = moduleKey,
                        notificationsEnabled = notificationsEnabled,
                        exactAlarmsAvailable = exactAlarmsAvailable,
                        onOpenExactAlarmSettings = { ReminderScheduler.openExactAlarmSettings(context) },
                        onSave = { isEnabled, intervalHours, hour, minute, customMessage ->
                            viewModel.saveNotificationPref(
                                module = moduleKey,
                                isEnabled = isEnabled,
                                intervalHours = intervalHours,
                                hour = hour,
                                minute = minute,
                                customMessage = customMessage
                            )
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
fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = WhiteMedium)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WhiteHigh)
    }
}

@Composable
fun NotificationStatusCard(
    notificationsEnabled: Boolean,
    exactAlarmsAvailable: Boolean,
    onOpenExactAlarmSettings: () -> Unit,
    onTestNotification: () -> Unit = {}
) {
    GlassCard(elevation = GlassElevation.Medium) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (notificationsEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (notificationsEnabled) SuccessColor else ErrorColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (notificationsEnabled) "Notifications are enabled" else "Notifications are blocked",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WhiteHigh
                )
            }
            if (!notificationsEnabled) {
                Text(
                    "Allow notifications in Android settings so reminders can appear.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorColor
                )
            }
            if (notificationsEnabled) {
                GlassOutlineButton(
                    onClick = onTestNotification,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send test notification")
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmsAvailable) {
                GlassDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = WarningColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Exact alarms disabled",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarningColor
                    )
                }
                Text(
                    "Reminders may be delayed. Enable exact alarms for reliable timing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteMedium
                )
                GlassOutlineButton(onClick = onOpenExactAlarmSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Open alarm settings")
                }
            }
        }
    }
}

@Composable
fun NotificationPrefCard(
    moduleLabel: String,
    pref: NotificationPrefEntity?,
    moduleKey: String,
    notificationsEnabled: Boolean,
    exactAlarmsAvailable: Boolean,
    onOpenExactAlarmSettings: () -> Unit,
    onSave: (Boolean, Int, Int, Int, String) -> Unit
) {
    var isEnabled by remember(pref) { mutableStateOf(pref?.isEnabled ?: false) }
    var intervalHours by remember(pref) { mutableStateOf(pref?.intervalHours ?: 2) }
    var reminderHour by remember(pref) { mutableStateOf(pref?.reminderHour ?: 8) }
    var reminderMinute by remember(pref) { mutableStateOf(pref?.reminderMinute ?: 0) }
    var customMessage by remember(pref) { mutableStateOf(pref?.customMessage ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    GlassCard(elevation = GlassElevation.Medium) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    moduleLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WhiteHigh,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        isEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentBlue,
                        checkedTrackColor = AccentBlue.copy(alpha = 0.3f),
                        uncheckedThumbColor = WhiteLow,
                        uncheckedTrackColor = GlassThin
                    )
                )
            }
            if (isEnabled) {
                Text(
                    "Repeat: every $intervalHours hours",
                    style = MaterialTheme.typography.labelSmall,
                    color = WhiteMedium
                )
                Slider(
                    value = intervalHours.toFloat(),
                    onValueChange = { intervalHours = it.toInt() },
                    valueRange = 1f..24f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        activeTrackColor = AccentBlue,
                        inactiveTrackColor = GlassThin,
                        thumbColor = AccentBlue
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassTextField(
                        value = reminderHour.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { reminderHour = it.coerceIn(0, 23) }
                        },
                        label = "Hour",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    GlassTextField(
                        value = reminderMinute.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { reminderMinute = it.coerceIn(0, 59) }
                        },
                        label = "Minute",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                GlassTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = "Custom message (optional)",
                    modifier = Modifier.fillMaxWidth()
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmsAvailable) {
                    Text(
                        "Enable exact alarms for reliable timing.",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningColor
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsEnabled) {
                    Text(
                        "Enable notifications first.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorColor
                    )
                }
                GlassPrimaryButton(
                    onClick = {
                        isSaving = true
                        onSave(isEnabled, intervalHours, reminderHour, reminderMinute, customMessage)
                        isSaving = false
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = WhitePure,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save reminder")
                    }
                }
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
        containerColor = GlassHeavy,
        title = { Text("Edit Profile & Goals", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = "Weight (kg)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = "Height (cm)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = "Age (years)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = calorieGoalText,
                    onValueChange = { calorieGoalText = it },
                    label = "Daily Calorie Goal (kcal)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Water goal: weight x 35 ml",
                    style = MaterialTheme.typography.labelSmall,
                    color = WhiteLow
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(onClick = {
                onSave(
                    name,
                    weightText.toFloatOrNull() ?: 70f,
                    heightText.toFloatOrNull() ?: 170f,
                    ageText.toIntOrNull() ?: 25,
                    calorieGoalText.toIntOrNull() ?: 2000
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
