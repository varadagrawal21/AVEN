package com.lifetracker.ui.books

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.data.db.BookEntity
import com.lifetracker.data.db.BookNoteEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.*
import androidx.compose.ui.window.DialogProperties
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
            GlassTopAppBar(title = "Books & Growth")
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Add Book",
                accentColor = BooksIndigo
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
                    title = "Books Completed",
                    value = "${state.completedCount}",
                    subtitle = "${state.readingBooks.size} reading",
                    icon = Icons.Default.LibraryBooks,
                    accentColor = BooksIndigo,
                    onClick = null
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
                        message = "No books yet. Add your first book!",
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
    GlassListItem(onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        book.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WhiteHigh,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (book.author.isNotEmpty()) {
                        Text(book.author, style = MaterialTheme.typography.labelSmall, color = WhiteMedium)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorColor)
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
                        style = MaterialTheme.typography.labelSmall,
                        color = WhiteMedium
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = BooksIndigo,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = BooksIndigo,
                    trackColor = GlassThin
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
        containerColor = GlassHeavy,
        title = { Text("Add Book", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title *",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = "Author",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = pagesText,
                    onValueChange = { pagesText = it },
                    label = "Total Pages",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                GlassOutlineButton(
                    onClick = { pdfLauncher.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = WhiteHigh)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (pdfUri.isEmpty()) "Attach PDF" else "PDF attached", color = WhiteHigh)
                }
            }
        },
        confirmButton = {
            GlassPrimaryButton(onClick = {
                if (title.isNotBlank()) {
                    onAdd(title, author, pagesText.toIntOrNull() ?: 0, pdfUri)
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
            GlassTopAppBar(
                title = book?.title ?: "Book Detail",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhiteHigh)
                    }
                },
                actions = {
                    book?.let { b ->
                        if (b.pdfUri.isNotEmpty()) {
                            IconButton(onClick = { navController.navigate(Screen.PdfViewer.createRoute(bookId)) }) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "Open PDF", tint = WhiteHigh)
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showAddNoteDialog = true },
                icon = Icons.Default.NoteAdd,
                contentDescription = "Add Note",
                accentColor = BooksIndigo
            )
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
                    GlassCard(elevation = GlassElevation.Medium) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                b.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = WhiteHigh
                            )
                            if (b.author.isNotEmpty()) {
                                Text(b.author, style = MaterialTheme.typography.bodyMedium, color = WhiteMedium)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (b.totalPages > 0) {
                                val progress = viewModel.getReadingProgress(b)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${b.pagesRead} / ${b.totalPages} pages", style = MaterialTheme.typography.labelSmall, color = WhiteMedium)
                                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = BooksIndigo, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = BooksIndigo,
                                    trackColor = GlassThin
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            GlassOutlineButton(
                                onClick = { showUpdatePagesDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Update Progress")
                            }
                        }
                    }
                }

                item { SectionHeader(title = "Notes & Learnings") }

                if (state.bookNotes.isEmpty()) {
                    item {
                        EmptyStateView(
                            message = "No notes yet. Tap + to add learnings.",
                            icon = Icons.Default.NoteAdd
                        )
                    }
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
    GlassListItem(onClick = null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(note.noteType, style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = GlassThin,
                            labelColor = WhiteHigh
                        )
                    )
                    if (note.pageNumber > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("p.${note.pageNumber}", style = MaterialTheme.typography.labelSmall, color = WhiteMedium)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(note.content, style = MaterialTheme.typography.bodyMedium, color = WhiteHigh)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorColor)
            }
        }
    }
}

@Composable
fun UpdatePagesDialog(
    currentPages: Int,
    totalPages: Int,
    onDismiss: () -> Unit,
    onUpdate: (Int) -> Unit
) {
    var pagesText by remember { mutableStateOf(currentPages.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassHeavy,
        title = { Text("Update Reading Progress", color = WhiteHigh) },
        text = {
            GlassTextField(
                value = pagesText,
                onValueChange = { pagesText = it },
                label = "Pages Read (of $totalPages)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            GlassPrimaryButton(
                onClick = { onUpdate(pagesText.toIntOrNull() ?: currentPages) }
            ) { Text("Update") }
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
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var noteType by remember { mutableStateOf("LEARNING") }
    var pageText by remember { mutableStateOf("") }
    val noteTypes = listOf("LEARNING", "SUMMARY", "QUOTE")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassHeavy,
        title = { Text("Add Note", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    noteTypes.forEach { type ->
                        FilterChip(
                            selected = noteType == type,
                            onClick = { noteType = type },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall, color = WhiteHigh) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = GlassThin,
                                selectedContainerColor = BooksIndigo.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
                GlassTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = "Note *",
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = pageText,
                    onValueChange = { pageText = it },
                    label = "Page number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(
                onClick = {
                    if (content.isNotBlank()) onAdd(content, noteType, pageText.toIntOrNull() ?: 0)
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
            GlassTopAppBar(
                title = "PDF Viewer",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhiteHigh)
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
                    NativePdfViewer(uriString = b.pdfUri)
                } else {
                    Text("No PDF attached to this book.", color = WhiteMedium)
                }
            } ?: CircularProgressIndicator(color = BooksIndigo)
        }
    }
}

@Composable
fun NativePdfViewer(uriString: String) {
    val context = LocalContext.current
    var pageCount by remember { mutableStateOf(0) }
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uriString) {
        try {
            val uri = Uri.parse(uriString)
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                fileDescriptor = pfd
                val pdfRenderer = PdfRenderer(pfd)
                renderer = pdfRenderer
                pageCount = pdfRenderer.pageCount
            } else {
                errorMessage = "Could not open PDF file."
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Error loading PDF"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                renderer?.close()
                fileDescriptor?.close()
            } catch (_: Exception) { }
        }
    }

    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: $errorMessage", color = ErrorColor)
        }
    } else if (pageCount == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BooksIndigo)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pageCount) { pageIndex ->
                PdfPageCard(renderer = renderer, pageIndex = pageIndex)
            }
        }
    }
}

@Composable
fun PdfPageCard(renderer: PdfRenderer?, pageIndex: Int) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(renderer, pageIndex) {
        if (renderer != null) {
            try {
                val page = renderer.openPage(pageIndex)
                val width = page.width * 2
                val height = page.height * 2
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                bitmap = bmp
            } catch (_: Exception) { }
        }
    }

    GlassCard(elevation = GlassElevation.Medium) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Page ${pageIndex + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(bitmap!!.width.toFloat() / bitmap!!.height.toFloat())
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BooksIndigo)
                }
            }
        }
    }
}
