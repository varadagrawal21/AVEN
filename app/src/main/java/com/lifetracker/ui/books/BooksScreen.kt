package com.lifetracker.ui.books

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lifetracker.data.db.BookEntity
import com.lifetracker.data.db.BookNoteEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.ui.theme.BooksIndigo
import com.lifetracker.viewmodel.BooksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    navController: NavController,
    viewModel: BooksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<BookEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("📚 Books & Growth") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BooksIndigo
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
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
            item {
                StatCard(
                    title = "Books Completed",
                    value = "${state.completedCount}",
                    subtitle = "${state.readingBooks.size} currently reading",
                    icon = Icons.Default.LibraryBooks,
                    accentColor = BooksIndigo
                )
            }

            if (state.readingBooks.isNotEmpty()) {
                item { SectionHeader(title = "Currently Reading") }
                items(state.readingBooks) { book ->
                    BookItem(
                        book = book,
                        onClick = { navController.navigate(Screen.BookDetail.createRoute(book.id)) },
                        onDelete = { bookToDelete = book },
                        progress = viewModel.getReadingProgress(book)
                    )
                }
            }

            if (state.completedBooks.isNotEmpty()) {
                item { SectionHeader(title = "Completed") }
                items(state.completedBooks) { book ->
                    BookItem(
                        book = book,
                        onClick = { navController.navigate(Screen.BookDetail.createRoute(book.id)) },
                        onDelete = { bookToDelete = book },
                        progress = 1f
                    )
                }
            }

            if (state.allBooks.isEmpty()) {
                item {
                    EmptyStateView(
                        message = "No books yet. Add your first book! 📚",
                        icon = Icons.Default.LibraryBooks
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddBookDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, author, pages, pdfUri ->
                viewModel.addBook(title, author, pages, pdfUri)
                showAddDialog = false
            }
        )
    }

    bookToDelete?.let { book ->
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteBook(book)
                bookToDelete = null
            },
            onDismiss = { bookToDelete = null }
        )
    }
}

@Composable
fun BookItem(
    book: BookEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    progress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (book.author.isNotEmpty()) {
                        Text(book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            if (book.totalPages > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${book.pagesRead} / ${book.totalPages} pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = BooksIndigo,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = BooksIndigo
                )
            }
        }
    }
}

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var pagesText by remember { mutableStateOf("") }
    var pdfUri by remember { mutableStateOf("") }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        pdfUri = uri?.toString() ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author, onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pagesText, onValueChange = { pagesText = it },
                    label = { Text("Total Pages") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { pdfLauncher.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (pdfUri.isEmpty()) "Attach PDF (optional)" else "PDF attached ✓")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    onAdd(title, author, pagesText.toIntOrNull() ?: 0, pdfUri)
                }
            }) { Text("Add") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    navController: NavController,
    viewModel: BooksViewModel = hiltViewModel()
) {
    var book by remember { mutableStateOf<BookEntity?>(null) }
    var showUpdatePagesDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(bookId) {
        book = viewModel.getBookById(bookId)
        viewModel.loadBookNotes(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Book Detail", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    book?.let { b ->
                        if (b.pdfUri.isNotEmpty()) {
                            IconButton(onClick = { navController.navigate(Screen.PdfViewer.createRoute(bookId)) }) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "Open PDF")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddNoteDialog = true }, containerColor = BooksIndigo) {
                Icon(Icons.Default.NoteAdd, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        book?.let { b ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(b.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (b.author.isNotEmpty()) Text(b.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (b.totalPages > 0) {
                                val progress = viewModel.getReadingProgress(b)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${b.pagesRead} / ${b.totalPages} pages", style = MaterialTheme.typography.bodySmall)
                                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = BooksIndigo, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = BooksIndigo)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            OutlinedButton(onClick = { showUpdatePagesDialog = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Update Progress")
                            }
                        }
                    }
                }

                item { SectionHeader(title = "Notes & Learnings") }

                if (state.bookNotes.isEmpty()) {
                    item { EmptyStateView(message = "No notes yet. Tap + to add learnings.", icon = Icons.Default.NoteAdd) }
                } else {
                    items(state.bookNotes) { note ->
                        BookNoteItem(note = note, onDelete = { viewModel.deleteNote(note) })
                    }
                }
            }
        }
    }

    if (showUpdatePagesDialog) {
        book?.let { b ->
            UpdatePagesDialog(
                currentPages = b.pagesRead,
                totalPages = b.totalPages,
                onDismiss = { showUpdatePagesDialog = false },
                onUpdate = { pages ->
                    viewModel.updatePagesRead(b, pages)
                    showUpdatePagesDialog = false
                }
            )
        }
    }

    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onAdd = { content, noteType, page ->
                viewModel.addNote(bookId, content, noteType, page)
                showAddNoteDialog = false
            }
        )
    }
}

@Composable
fun BookNoteItem(note: BookNoteEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(note.noteType, style = MaterialTheme.typography.labelSmall) })
                    if (note.pageNumber > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("p.${note.pageNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(note.content, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun UpdatePagesDialog(currentPages: Int, totalPages: Int, onDismiss: () -> Unit, onUpdate: (Int) -> Unit) {
    var pagesText by remember { mutableStateOf(currentPages.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Reading Progress") },
        text = {
            OutlinedTextField(
                value = pagesText, onValueChange = { pagesText = it },
                label = { Text("Pages Read (of $totalPages)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onUpdate(pagesText.toIntOrNull() ?: currentPages) }) { Text("Update") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String, String, Int) -> Unit) {
    var content by remember { mutableStateOf("") }
    var noteType by remember { mutableStateOf("LEARNING") }
    var pageText by remember { mutableStateOf("") }
    val noteTypes = listOf("LEARNING", "SUMMARY", "QUOTE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    noteTypes.forEach { type ->
                        FilterChip(
                            selected = noteType == type,
                            onClick = { noteType = type },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = content, onValueChange = { content = it },
                    label = { Text("Note *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pageText, onValueChange = { pageText = it },
                    label = { Text("Page number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (content.isNotBlank()) onAdd(content, noteType, pageText.toIntOrNull() ?: 0)
            }) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    bookId: Long,
    navController: NavController,
    viewModel: BooksViewModel = hiltViewModel()
) {
    var book by remember { mutableStateOf<BookEntity?>(null) }

    LaunchedEffect(bookId) {
        book = viewModel.getBookById(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Viewer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            book?.let { b ->
                if (b.pdfUri.isNotEmpty()) {
                    // AndroidPdfViewer is a View-based library; use AndroidView to embed it
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { context ->
                            com.github.barteksc.pdfviewer.PDFView(context, null).apply {
                                fromUri(Uri.parse(b.pdfUri))
                                    .enableSwipe(true)
                                    .swipeHorizontal(false)
                                    .enableDoubletap(true)
                                    .defaultPage(0)
                                    .load()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("No PDF attached to this book.")
                }
            } ?: CircularProgressIndicator()
        }
    }
}
