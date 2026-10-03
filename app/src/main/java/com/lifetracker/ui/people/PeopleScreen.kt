package com.lifetracker.ui.people

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.data.db.PersonEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.*
import androidx.compose.ui.window.DialogProperties
import com.lifetracker.viewmodel.PeopleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    navController: NavController,
    viewModel: PeopleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var personToDelete by remember { mutableStateOf<PersonEntity?>(null) }
    var personToEdit by remember { mutableStateOf<PersonEntity?>(null) }

    Scaffold(
        topBar = {
            GlassTopAppBar(title = "People")
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = Icons.Default.PersonAdd,
                contentDescription = "Add Person",
                accentColor = PeoplePink
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            GlassTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = "Search people...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.people.isEmpty()) {
                EmptyStateView(
                    message = "No people added yet. Tap + to add someone.",
                    icon = Icons.Default.People
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.people) { person ->
                        PersonItem(
                            person = person,
                            onClick = { navController.navigate(Screen.PersonDetail.createRoute(person.id)) },
                            onEdit = { personToEdit = person },
                            onDelete = { personToDelete = person }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPersonDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, relationship, character, behaviour, relationshipNotes, contact ->
                viewModel.addPerson(name, relationship, character, behaviour, relationshipNotes, contact)
                showAddDialog = false
            }
        )
    }

    personToEdit?.let { person ->
        AddOrEditPersonDialog(
            existingPerson = person,
            onDismiss = { personToEdit = null },
            onSave = { name, relationship, character, behaviour, relationshipNotes, contact ->
                viewModel.updatePerson(
                    person.copy(
                        name = name,
                        relationship = relationship,
                        characterNotes = character,
                        behaviourNotes = behaviour,
                        relationshipNotes = relationshipNotes,
                        contactInfo = contact
                    )
                )
                personToEdit = null
            }
        )
    }

    personToDelete?.let { person ->
        ConfirmDeleteDialog(
            onConfirm = { viewModel.deletePerson(person); personToDelete = null },
            onDismiss = { personToDelete = null }
        )
    }
}

@Composable
fun PersonItem(
    person: PersonEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
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
                        .background(PeoplePink.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = PeoplePink,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        person.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WhiteHigh
                    )
                    if (person.relationship.isNotEmpty()) {
                        Text(
                            person.relationship,
                            style = MaterialTheme.typography.labelSmall,
                            color = WhiteMedium
                        )
                    }
                    if (person.characterNotes.isNotEmpty()) {
                        Text(
                            person.characterNotes,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = WhiteLow
                        )
                    }
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentBlue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorColor)
                }
            }
        }
    }
}

@Composable
fun AddOrEditPersonDialog(
    existingPerson: PersonEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(existingPerson?.name ?: "") }
    var relationship by remember { mutableStateOf(existingPerson?.relationship ?: "") }
    var character by remember { mutableStateOf(existingPerson?.characterNotes ?: "") }
    var behaviour by remember { mutableStateOf(existingPerson?.behaviourNotes ?: "") }
    var relationshipNotes by remember { mutableStateOf(existingPerson?.relationshipNotes ?: "") }
    var contact by remember { mutableStateOf(existingPerson?.contactInfo ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassHeavy,
        title = { Text(if (existingPerson != null) "Edit Person" else "Add Person", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name *",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = "Relationship",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = character,
                    onValueChange = { character = it },
                    label = "Character notes",
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = behaviour,
                    onValueChange = { behaviour = it },
                    label = "Behaviour notes",
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = relationshipNotes,
                    onValueChange = { relationshipNotes = it },
                    label = "Relationship dynamics",
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = "Contact info",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(onClick = {
                if (name.isNotBlank()) onSave(name, relationship, character, behaviour, relationshipNotes, contact)
            }) { Text(if (existingPerson != null) "Save" else "Add") }
        },
        dismissButton = {
            GlassOutlineButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = GlassShapes.Large,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

@Composable
fun AddPersonDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String) -> Unit
) {
    AddOrEditPersonDialog(
        existingPerson = null,
        onDismiss = onDismiss,
        onSave = onAdd
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: Long,
    navController: NavController,
    viewModel: PeopleViewModel = hiltViewModel()
) {
    var person by remember { mutableStateOf<PersonEntity?>(null) }

    LaunchedEffect(personId) {
        person = viewModel.getPersonById(personId)
    }

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = person?.name ?: "Person Detail",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhiteHigh)
                    }
                }
            )
        }
    ) { padding ->
        person?.let { p ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GlassCard(elevation = GlassElevation.Medium) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(PeoplePink.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PeoplePink,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    p.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteHigh
                                )
                                if (p.relationship.isNotEmpty()) {
                                    Text(p.relationship, style = MaterialTheme.typography.bodyMedium, color = WhiteMedium)
                                }
                                if (p.contactInfo.isNotEmpty()) {
                                    Text(p.contactInfo, style = MaterialTheme.typography.labelSmall, color = WhiteLow)
                                }
                            }
                        }
                    }
                }
                if (p.characterNotes.isNotEmpty()) {
                    item {
                        Text(
                            "Character",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PeoplePink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(p.characterNotes, style = MaterialTheme.typography.bodyMedium, color = WhiteHigh)
                    }
                }
                if (p.behaviourNotes.isNotEmpty()) {
                    item {
                        Text(
                            "Behaviour",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PeoplePink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(p.behaviourNotes, style = MaterialTheme.typography.bodyMedium, color = WhiteHigh)
                    }
                }
                if (p.relationshipNotes.isNotEmpty()) {
                    item {
                        Text(
                            "Relationship Dynamics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PeoplePink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(p.relationshipNotes, style = MaterialTheme.typography.bodyMedium, color = WhiteHigh)
                    }
                }
            }
        }
    }
}
