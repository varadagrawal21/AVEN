package com.lifetracker.ui.academic

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
import com.lifetracker.data.db.AcademicSessionEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.*
import androidx.compose.ui.window.DialogProperties
import com.lifetracker.viewmodel.AcademicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicScreen(
    viewModel: AcademicViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<AcademicSessionEntity?>(null) }

    Scaffold(
        topBar = {
            GlassTopAppBar(title = "Academic")
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Log Study Session",
                accentColor = AcademicOrange
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
            item {
                DashboardStatCard(
                    title = "Today's Study Time",
                    value = "${state.todayTotalMinutes}",
                    subtitle = "minutes",
                    icon = Icons.Default.Timer,
                    accentColor = AcademicOrange,
                    onClick = null
                )
            }

            if (state.subjectTimes.isNotEmpty()) {
                item {
                    SectionHeader(title = "Time per Subject")
                }
                items(state.subjectTimes) { subjectTime ->
                    val maxMinutes = state.subjectTimes.maxOf { it.totalMinutes }.toFloat()
                    val progress = if (maxMinutes > 0) subjectTime.totalMinutes / maxMinutes else 0f
                    GlassCard(elevation = GlassElevation.Thin) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    subjectTime.subject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteHigh
                                )
                                Text(
                                    "${subjectTime.totalMinutes / 60}h ${subjectTime.totalMinutes % 60}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AcademicOrange
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = AcademicOrange,
                                trackColor = GlassThin,
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Today's Sessions")
            }
            if (state.todaySessions.isEmpty()) {
                item {
                    EmptyStateView(
                        message = "No study sessions today. Start studying!",
                        icon = Icons.Default.School
                    )
                }
            } else {
                items(state.todaySessions) { session ->
                    AcademicSessionItem(session = session, onDelete = { sessionToDelete = session })
                }
            }
        }
    }

    if (showAddDialog) {
        AddStudySessionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { subject, duration, topic, notes ->
                viewModel.addSession(subject, duration, topic, notes)
                showAddDialog = false
            }
        )
    }

    sessionToDelete?.let { session ->
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteSession(session)
                sessionToDelete = null
            },
            onDismiss = { sessionToDelete = null }
        )
    }
}

@Composable
fun AcademicSessionItem(session: AcademicSessionEntity, onDelete: () -> Unit) {
    GlassListItem(onClick = null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.subject,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WhiteHigh
                )
                Text(
                    "${session.durationMinutes} min${if (session.topic.isNotEmpty()) " - ${session.topic}" else ""}",
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
fun AddStudySessionDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassHeavy,
        title = { Text("Log Study Session", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = "Subject *",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = "Duration (minutes) *",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = "Topic",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(onClick = {
                val duration = durationText.toIntOrNull() ?: 0
                if (subject.isNotBlank() && duration > 0) {
                    onAdd(subject, duration, topic, notes)
                }
            }) { Text("Add") }
        },
        dismissButton = {
            GlassOutlineButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = GlassShapes.Large,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}
