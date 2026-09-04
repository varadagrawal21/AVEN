package com.lifetracker.ui.custom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lifetracker.data.db.CustomEntryEntity
import com.lifetracker.data.db.CustomSectionEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.ui.theme.CustomGray
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
            TopAppBar(title = { Text("⚙️ Custom Sections") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = CustomGray) {
                Icon(Icons.Default.Add, contentDescription = "Add Section")
            }
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
fun CustomSectionItem(section: CustomSectionEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(section.icon, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(section.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (section.description.isNotEmpty()) {
                        Text(section.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
    var icon by remember { mutableStateOf("📝") }
    var color by remember { mutableStateOf("#2196F3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Section") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Section Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = icon, onValueChange = { icon = it }, label = { Text("Icon (emoji)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onAdd(name, description, icon, color)
            }) { Text("Create") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
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
            TopAppBar(
                title = { Text(section?.let { "${it.icon} ${it.name}" } ?: "Section") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddEntryDialog = true }, containerColor = CustomGray) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { padding ->
        if (state.selectedSectionEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyStateView(message = "No entries yet. Tap + to add.", icon = Icons.Default.NoteAdd)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.selectedSectionEntries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                if (entry.content.isNotEmpty()) Text(entry.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(entry.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { entryToDelete = entry }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEntryDialog) {
        AlertDialog(
            onDismissRequest = { showAddEntryDialog = false },
            title = { Text("Add Entry") },
            text = {
                var title by remember { mutableStateOf("") }
                var content by remember { mutableStateOf("") }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {},
            dismissButton = { OutlinedButton(onClick = { showAddEntryDialog = false }) { Text("Cancel") } }
        )
    }

    entryToDelete?.let { entry ->
        ConfirmDeleteDialog(
            onConfirm = { viewModel.deleteEntry(entry); entryToDelete = null },
            onDismiss = { entryToDelete = null }
        )
    }
}
