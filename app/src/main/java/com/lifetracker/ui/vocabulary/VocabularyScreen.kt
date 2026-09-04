package com.lifetracker.ui.vocabulary

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
import com.lifetracker.data.db.VocabularyEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.ui.theme.VocabPurple
import com.lifetracker.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    navController: NavController,
    viewModel: VocabularyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var wordToDelete by remember { mutableStateOf<VocabularyEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("📖 Vocabulary (${state.wordCount} words)") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = VocabPurple
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Word")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search words...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (state.words.isEmpty()) {
                EmptyStateView(
                    message = if (state.searchQuery.isEmpty())
                        "No words yet. Tap + to add your first word!"
                    else "No words found for '${state.searchQuery}'",
                    icon = Icons.Default.MenuBook
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.words) { word ->
                        VocabWordItem(
                            word = word,
                            onClick = { navController.navigate(Screen.VocabDetail.createRoute(word.id)) },
                            onDelete = { wordToDelete = word }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWordDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { word, meaning, example, pos ->
                viewModel.addWord(word, meaning, example, pos)
                showAddDialog = false
            }
        )
    }

    wordToDelete?.let { word ->
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteWord(word)
                wordToDelete = null
            },
            onDismiss = { wordToDelete = null }
        )
    }
}

@Composable
fun VocabWordItem(
    word: VocabularyEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        word.word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VocabPurple
                    )
                    if (word.partOfSpeech.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "(${word.partOfSpeech})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    word.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    word.dateAdded,
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
fun AddWordDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var partOfSpeech by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Word") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = word, onValueChange = { word = it },
                    label = { Text("Word *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = meaning, onValueChange = { meaning = it },
                    label = { Text("Meaning *") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = example, onValueChange = { example = it },
                    label = { Text("Example sentence") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = partOfSpeech, onValueChange = { partOfSpeech = it },
                    label = { Text("Part of speech (noun, verb...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (word.isNotBlank() && meaning.isNotBlank()) {
                        onAdd(word, meaning, example, partOfSpeech)
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabDetailScreen(
    wordId: Long,
    navController: NavController,
    viewModel: VocabularyViewModel = hiltViewModel()
) {
    var word by remember { mutableStateOf<VocabularyEntity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(wordId) {
        word = viewModel.getWordById(wordId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(word?.word ?: "Word Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        word?.let { w ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(w.word, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = VocabPurple)
                if (w.partOfSpeech.isNotEmpty()) {
                    Text("(${w.partOfSpeech})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Divider()
                Text("Meaning", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(w.meaning, style = MaterialTheme.typography.bodyLarge)
                if (w.exampleSentence.isNotEmpty()) {
                    Divider()
                    Text("Example", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(w.exampleSentence, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Added: ${w.dateAdded}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
