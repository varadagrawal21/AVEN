package com.lifetracker.ui.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.data.db.CustomEntryEntity
import com.lifetracker.data.db.CustomSectionEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.*
import androidx.compose.ui.window.DialogProperties
import com.lifetracker.viewmodel.CustomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomScreen(
    navController: NavController,
    viewModel: CustomViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var sectionToDelete by remember { mutableStateOf<CustomSectionEntity?>(null) }

    Scaffold(
        topBar = {
            GlassTopAppBar(title = "Custom Sections")
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Add Section",
                accentColor = CustomGray
            )
        }
    ) { padding ->
        if (state.sections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    message = "No custom sections yet. Create your own tracking section!",
                    icon = Icons.Default.GridView
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.sections) { section ->
                    CustomSectionItem(
                        section = section,
                        onClick = { navController.navigate(Screen.CustomSectionDetail.createRoute(section.id)) },
                        onDelete = { sectionToDelete = section }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSectionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, description, icon, color ->
                viewModel.addSection(name, description, icon, color)
                showAddDialog = false
            }
        )
    }

    sectionToDelete?.let { section ->
        ConfirmDeleteDialog(
            onConfirm = { viewModel.deleteSection(section); sectionToDelete = null },
            onDismiss = { sectionToDelete = null }
        )
    }
}

@Composable
fun CustomSectionItem(
    section: CustomSectionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    GlassListItem(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CustomGray.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(section.icon, style = MaterialTheme.typography.titleMedium, color = CustomGray)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        section.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WhiteHigh
                    )
                    if (section.description.isNotEmpty()) {
                        Text(
                            section.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = WhiteMedium
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = WhiteMinimal)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorColor)
                }
            }
        }
    }
}

@Composable
fun AddSectionDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#2196F3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassHeavy,
        title = { Text("Create Custom Section", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Section Name *",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = "Icon (emoji)",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(onClick = {
                if (name.isNotBlank()) onAdd(name, description, icon, color)
            }) { Text("Create") }
        },
        dismissButton = {
            GlassOutlineButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = GlassShapes.Large,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSectionDetailScreen(
    sectionId: Long,
    navController: NavController,
    viewModel: CustomViewModel = hiltViewModel()
) {
    var section by remember { mutableStateOf<CustomSectionEntity?>(null) }
    val state by viewModel.state.collectAsState()
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<CustomEntryEntity?>(null) }

    LaunchedEffect(sectionId) {
        section = viewModel.getSectionById(sectionId)
        viewModel.loadEntriesForSection(sectionId)
    }

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = section?.let { "${it.icon} ${it.name}" } ?: "Section",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhiteHigh)
                    }
                }
            )
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showAddEntryDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Add Entry",
                accentColor = CustomGray
            )
        }
    ) { padding ->
        if (state.selectedSectionEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    message = "No entries yet. Tap + to add.",
                    icon = Icons.Default.NoteAdd
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.selectedSectionEntries) { entry ->
                    GlassListItem(onClick = null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteHigh
                                )
                                if (entry.content.isNotEmpty()) {
                                    Text(entry.content, style = MaterialTheme.typography.bodySmall, color = WhiteMedium)
                                }
                                Text(entry.date, style = MaterialTheme.typography.labelSmall, color = WhiteLow)
                            }
                            IconButton(onClick = { entryToDelete = entry }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorColor)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEntryDialog) {
        AddEntryDialog(
            sectionId = sectionId,
            onDismiss = { showAddEntryDialog = false },
            onAdd = { title, content ->
                viewModel.addEntry(sectionId, title, content)
                showAddEntryDialog = false
            }
        )
    }

    entryToDelete?.let { entry ->
        ConfirmDeleteDialog(
            onConfirm = { viewModel.deleteEntry(entry); entryToDelete = null },
            onDismiss = { entryToDelete = null }
        )
    }
}

@Composable
private fun AddEntryDialog(
    sectionId: Long,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassHeavy,
        title = { Text("Add Entry", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title *",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = "Content",
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(onClick = {
                if (title.isNotBlank()) onAdd(title, content)
            }) { Text("Add") }
        },
        dismissButton = {
            GlassOutlineButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = GlassShapes.Large,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}
