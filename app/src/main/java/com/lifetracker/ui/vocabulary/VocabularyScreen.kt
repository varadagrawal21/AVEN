package com.lifetracker.ui.vocabulary

import android.media.MediaPlayer
import android.net.Uri
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
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.data.db.VocabularyEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.*
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import com.lifetracker.data.dictionary.DictionaryEntry
import com.lifetracker.viewmodel.VocabularyViewModel
import androidx.compose.material.icons.automirrored.filled.ArrowBack

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
            GlassTopAppBar(
                title = "Vocabulary (${state.wordCount})",
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Word", tint = WhiteHigh)
                    }
                }
            )
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Add Word",
                accentColor = VocabPurple
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GlassTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = "Search words...",
                leadingIcon = Icons.Default.Search,
                trailingIcon = if (state.searchQuery.isNotEmpty()) Icons.Default.Clear else null,
                onTrailingIconClick = if (state.searchQuery.isNotEmpty()) {
                    { viewModel.setSearchQuery("") }
                } else null,
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
            lookupResult = state.dictionaryEntry,
            isLookingUp = state.isLookingUp,
            lookupError = state.lookupError,
            onLookup = viewModel::lookupWord,
            onDismiss = {
                showAddDialog = false
                viewModel.clearDictionaryEntry()
            },
            onAdd = { entry ->
                viewModel.addWord(entry)
                showAddDialog = false
                viewModel.clearDictionaryEntry()
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
    GlassListItem(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                            style = MaterialTheme.typography.labelSmall,
                            color = WhiteMedium
                        )
                    }
                }
                Text(
                    word.meaning,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = WhiteMedium
                )
                Text(
                    word.dateAdded,
                    style = MaterialTheme.typography.labelSmall,
                    color = WhiteLow
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorColor)
            }
        }
    }
}

@Composable
fun AddWordDialog(
    lookupResult: DictionaryEntry? = null,
    isLookingUp: Boolean = false,
    lookupError: String? = null,
    onLookup: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onAdd: (DictionaryEntry) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var partOfSpeech by remember { mutableStateOf("") }

    LaunchedEffect(lookupResult) {
        lookupResult?.let { result ->
            word = result.word
            meaning = result.meaning
            example = result.exampleSentence
            partOfSpeech = result.partOfSpeech
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassHeavy,
        title = { Text("Add New Word", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = "Word *",
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    GlassOutlineButton(
                        onClick = { onLookup(word) },
                        enabled = word.isNotBlank() && !isLookingUp
                    ) {
                        Text(if (isLookingUp) "Looking up..." else "Look up")
                    }
                }
                lookupError?.let { error ->
                    Text(error, style = MaterialTheme.typography.labelSmall, color = ErrorColor)
                }
                GlassTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = "Meaning *",
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = example,
                    onValueChange = { example = it },
                    label = "Example sentence",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = partOfSpeech,
                    onValueChange = { partOfSpeech = it },
                    label = "Part of speech",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(
                onClick = {
                    if (word.isNotBlank() && meaning.isNotBlank()) {
                        onAdd(
                            DictionaryEntry(
                                word = word,
                                meaning = meaning,
                                exampleSentence = example,
                                partOfSpeech = partOfSpeech,
                                pronunciationAudioUrl = lookupResult?.pronunciationAudioUrl.orEmpty()
                            )
                        )
                    }
                }
            ) { Text("Add") }
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
fun VocabDetailScreen(
    wordId: Long,
    navController: NavController,
    viewModel: VocabularyViewModel = hiltViewModel()
) {
    var word by remember { mutableStateOf<VocabularyEntity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isAudioReady by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(wordId) {
        word = viewModel.getWordById(wordId)
    }

    val audioUrl = word?.pronunciationAudioUrl.orEmpty()
    val mediaPlayer = remember(audioUrl) { MediaPlayer() }
    DisposableEffect(audioUrl) {
        if (audioUrl.isNotBlank()) {
            mediaPlayer.setDataSource(context, Uri.parse(audioUrl))
            mediaPlayer.setOnPreparedListener {
                isAudioReady = true
            }
            mediaPlayer.setOnCompletionListener {
                isPlaying = false
            }
            mediaPlayer.prepareAsync()
        }
        onDispose {
            mediaPlayer.release()
            isAudioReady = false
            isPlaying = false
        }
    }

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = word?.word ?: "Word Detail",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhiteHigh)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WhiteHigh)
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
                GlassCard(elevation = GlassElevation.Medium) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            w.word,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = VocabPurple
                        )
                        if (w.partOfSpeech.isNotEmpty()) {
                            Text(
                                "(${w.partOfSpeech})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WhiteMedium
                            )
                        }
                        GlassDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Meaning",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = VocabPurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(w.meaning, style = MaterialTheme.typography.bodyLarge, color = WhiteHigh)
                        if (audioUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            GlassOutlineButton(
                                onClick = {
                                    if (isPlaying) {
                                        mediaPlayer.pause()
                                        isPlaying = false
                                    } else if (isAudioReady) {
                                        mediaPlayer.start()
                                        isPlaying = true
                                    }
                                },
                                enabled = isAudioReady
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isPlaying) "Stop" else "Play pronunciation")
                            }
                        }
                        if (w.exampleSentence.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            GlassDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Example",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = VocabPurple
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(w.exampleSentence, style = MaterialTheme.typography.bodyMedium, color = WhiteMedium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Added: ${w.dateAdded}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WhiteLow
                        )
                    }
                }
            }
        }
    }
}
