package com.lifetracker.ui.people

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lifetracker.data.db.PersonEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.ui.theme.PeoplePink
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("👥 People Room") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = PeoplePink) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Person")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search people...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.people.isEmpty()) {
                EmptyStateView(message = "No people added yet. Tap + to add someone.", icon = Icons.Default.People)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.people) { person ->
                        PersonItem(
                            person = person,
                            onClick = { navController.navigate(Screen.PersonDetail.createRoute(person.id)) },
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

    personToDelete?.let { person ->
        ConfirmDeleteDialog(
            onConfirm = { viewModel.deletePerson(person); personToDelete = null },
            onDismiss = { personToDelete = null }
        )
    }
}

@Composable
fun PersonItem(person: PersonEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PeoplePink, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(person.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (person.relationship.isNotEmpty()) {
                        Text(person.relationship, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (person.characterNotes.isNotEmpty()) {
                        Text(person.characterNotes, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddPersonDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var character by remember { mutableStateOf("") }
    var behaviour by remember { mutableStateOf("") }
    var relationshipNotes by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Person") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = relationship, onValueChange = { relationship = it }, label = { Text("Relationship (friend, mentor...)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = character, onValueChange = { character = it }, label = { Text("Character notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = behaviour, onValueChange = { behaviour = it }, label = { Text("Behaviour notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = relationshipNotes, onValueChange = { relationshipNotes = it }, label = { Text("Relationship dynamics") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact info") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onAdd(name, relationship, character, behaviour, relationshipNotes, contact)
            }) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
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
            TopAppBar(
                title = { Text(person?.name ?: "Person Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PeoplePink, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(p.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (p.relationship.isNotEmpty()) Text(p.relationship, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (p.contactInfo.isNotEmpty()) Text(p.contactInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (p.characterNotes.isNotEmpty()) {
                    item {
                        Text("Character", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PeoplePink)
                        Text(p.characterNotes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (p.behaviourNotes.isNotEmpty()) {
                    item {
                        Text("Behaviour", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PeoplePink)
                        Text(p.behaviourNotes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (p.relationshipNotes.isNotEmpty()) {
                    item {
                        Text("Relationship Dynamics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PeoplePink)
                        Text(p.relationshipNotes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
