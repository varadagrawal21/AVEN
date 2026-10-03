package com.lifetracker.ui.quotes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.data.db.QuoteEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.*
import androidx.compose.ui.window.DialogProperties
import com.lifetracker.viewmodel.QuotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    navController: NavController,
    viewModel: QuotesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var quoteToDelete by remember { mutableStateOf<QuoteEntity?>(null) }
    var quoteToEdit by remember { mutableStateOf<QuoteEntity?>(null) }

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = "Quotes (${state.quoteCount})",
                actions = {
                    IconButton(onClick = { viewModel.toggleFavoritesFilter() }) {
                        Icon(
                            if (state.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (state.showFavoritesOnly) ErrorColor else WhiteHigh
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Add Quote",
                accentColor = QuotesTeal
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            GlassTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = "Search quotes...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (state.quotes.isEmpty()) {
                EmptyStateView(
                    message = if (state.showFavoritesOnly) "No favorite quotes yet." else "No quotes yet. Add your first quote!",
                    icon = Icons.Default.FormatQuote
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.quotes) { quote ->
                        QuoteItem(
                            quote = quote,
                            onClick = { navController.navigate(Screen.QuoteDetail.createRoute(quote.id)) },
                            onFavorite = { viewModel.toggleFavorite(quote) },
                            onEdit = { quoteToEdit = quote },
                            onDelete = { quoteToDelete = quote }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddOrEditQuoteDialog(
            existingQuote = null,
            onDismiss = { showAddDialog = false },
            onSave = { text, author, source, reflection, howToApply, category ->
                viewModel.addQuote(text, author, source, reflection, howToApply, category)
                showAddDialog = false
            }
        )
    }

    quoteToEdit?.let { quote ->
        AddOrEditQuoteDialog(
            existingQuote = quote,
            onDismiss = { quoteToEdit = null },
            onSave = { text, author, source, reflection, howToApply, category ->
                viewModel.updateQuote(
                    quote.copy(
                        quoteText = text,
                        author = author,
                        source = source,
                        reflection = reflection,
                        howToApply = howToApply,
                        category = category
                    )
                )
                quoteToEdit = null
            }
        )
    }

    quoteToDelete?.let { quote ->
        ConfirmDeleteDialog(
            onConfirm = { viewModel.deleteQuote(quote); quoteToDelete = null },
            onDismiss = { quoteToDelete = null }
        )
    }
}

@Composable
fun QuoteItem(
    quote: QuoteEntity,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassListItem(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "\"${quote.quoteText}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = WhiteHigh
                )
                if (quote.author.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "- ${quote.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = WhiteMedium
                    )
                }
            }
            Row {
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (quote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (quote.isFavorite) ErrorColor else WhiteMedium
                    )
                }
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
fun AddOrEditQuoteDialog(
    existingQuote: QuoteEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var quoteText by remember { mutableStateOf(existingQuote?.quoteText ?: "") }
    var author by remember { mutableStateOf(existingQuote?.author ?: "") }
    var source by remember { mutableStateOf(existingQuote?.source ?: "") }
    var reflection by remember { mutableStateOf(existingQuote?.reflection ?: "") }
    var howToApply by remember { mutableStateOf(existingQuote?.howToApply ?: "") }
    var category by remember { mutableStateOf(existingQuote?.category ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassHeavy,
        title = { Text(if (existingQuote != null) "Edit Quote" else "Add Quote", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(
                    value = quoteText,
                    onValueChange = { quoteText = it },
                    label = "Quote *",
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = "Author",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = "Source",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = reflection,
                    onValueChange = { reflection = it },
                    label = "Your reflection",
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = howToApply,
                    onValueChange = { howToApply = it },
                    label = "How to apply",
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = "Category",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(onClick = {
                if (quoteText.isNotBlank()) onSave(quoteText, author, source, reflection, howToApply, category)
            }) { Text(if (existingQuote != null) "Save" else "Add") }
        },
        dismissButton = {
            GlassOutlineButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = GlassShapes.Large,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

@Composable
fun AddQuoteDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String) -> Unit
) {
    AddOrEditQuoteDialog(
        existingQuote = null,
        onDismiss = onDismiss,
        onSave = onAdd
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    quoteId: Long,
    navController: NavController,
    viewModel: QuotesViewModel = hiltViewModel()
) {
    var quote by remember { mutableStateOf<QuoteEntity?>(null) }

    LaunchedEffect(quoteId) {
        quote = viewModel.getQuoteById(quoteId)
    }

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = "Quote Detail",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhiteHigh)
                    }
                },
                actions = {
                    quote?.let { q ->
                        IconButton(onClick = { viewModel.toggleFavorite(q) }) {
                            Icon(
                                if (q.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (q.isFavorite) ErrorColor else WhiteHigh
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        quote?.let { q ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GlassCard(elevation = GlassElevation.Medium) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "\"${q.quoteText}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = WhiteHigh
                            )
                            if (q.author.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "- ${q.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteHigh
                                )
                            }
                            if (q.source.isNotEmpty()) {
                                Text(
                                    "Source: ${q.source}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WhiteMedium
                                )
                            }
                        }
                    }
                }
                if (q.reflection.isNotEmpty()) {
                    item {
                        Text(
                            "My Reflection",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = QuotesTeal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(q.reflection, style = MaterialTheme.typography.bodyMedium, color = WhiteHigh)
                    }
                }
                if (q.howToApply.isNotEmpty()) {
                    item {
                        Text(
                            "How I Apply This",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = QuotesTeal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(q.howToApply, style = MaterialTheme.typography.bodyMedium, color = WhiteHigh)
                    }
                }
            }
        }
    }
}
