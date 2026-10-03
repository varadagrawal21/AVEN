package com.lifetracker.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifetracker.data.db.FinanceLogEntity
import com.lifetracker.ui.components.*
import com.lifetracker.ui.theme.*
import androidx.compose.ui.window.DialogProperties
import com.lifetracker.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<FinanceLogEntity?>(null) }

    Scaffold(
        topBar = {
            GlassTopAppBar(title = "Finance")
        },
        floatingActionButton = {
            GlassFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Add Transaction",
                accentColor = FinanceGold
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
                GlassCard(elevation = GlassElevation.Medium) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "This Month",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WhiteMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatColumn("Income", "+${String.format("%.0f", state.monthIncome)}", SuccessColor)
                            StatColumn("Expenses", "-${String.format("%.0f", state.monthExpenses)}", ErrorColor)
                            StatColumn("Balance", "${String.format("%.0f", state.balance)}", if (state.balance >= 0) SuccessColor else ErrorColor)
                        }
                    }
                }
            }

            if (state.categoryBreakdown.isNotEmpty()) {
                item {
                    SectionHeader(title = "Expense Breakdown")
                }
                items(state.categoryBreakdown) { cat ->
                    GlassListItem(onClick = null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                cat.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = WhiteHigh
                            )
                            Text(
                                "${String.format("%.0f", cat.total)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ErrorColor
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Transactions")
            }
            if (state.logs.isEmpty()) {
                item {
                    EmptyStateView(
                        message = "No transactions this month. Tap + to add.",
                        icon = Icons.Default.AccountBalance
                    )
                }
            } else {
                items(state.logs) { log ->
                    FinanceLogItem(log = log, onDelete = { logToDelete = log })
                }
            }
        }
    }

    if (showAddDialog) {
        AddFinanceDialog(
            expenseCategories = state.expenseCategories,
            incomeCategories = state.incomeCategories,
            onDismiss = { showAddDialog = false },
            onAdd = { type, amount, category, description ->
                viewModel.addLog(type, amount, category, description)
                showAddDialog = false
            }
        )
    }

    logToDelete?.let { log ->
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.deleteLog(log)
                logToDelete = null
            },
            onDismiss = { logToDelete = null }
        )
    }
}

@Composable
fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = WhiteMedium)
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun FinanceLogItem(log: FinanceLogEntity, onDelete: () -> Unit) {
    val isIncome = log.type == "INCOME"
    GlassListItem(onClick = null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background((if (isIncome) SuccessColor else ErrorColor).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isIncome) SuccessColor else ErrorColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        log.category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WhiteHigh
                    )
                    Text(
                        "${log.date}${if (log.description.isNotEmpty()) " - ${log.description}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteMedium
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${if (isIncome) "+" else "-"}${String.format("%.0f", log.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) SuccessColor else ErrorColor
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorColor)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFinanceDialog(
    expenseCategories: List<String>,
    incomeCategories: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, String) -> Unit
) {
    var type by remember { mutableStateOf("EXPENSE") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = if (type == "INCOME") incomeCategories else expenseCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassHeavy,
        title = { Text("Add Transaction", color = WhiteHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = { type = "EXPENSE"; selectedCategory = "" },
                        label = { Text("Expense", color = WhiteHigh) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = GlassThin,
                            selectedContainerColor = GlassRegular
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = { type = "INCOME"; selectedCategory = "" },
                        label = { Text("Income", color = WhiteHigh) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = GlassThin,
                            selectedContainerColor = GlassRegular
                        )
                    )
                }
                GlassTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Amount",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    GlassTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        label = "Category",
                        trailingIcon = Icons.Default.ExpandMore,
                        onTrailingIconClick = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = WhiteHigh) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                GlassTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (optional)"
                )
            }
        },
        confirmButton = {
            GlassPrimaryButton(onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount > 0 && selectedCategory.isNotEmpty()) {
                    onAdd(type, amount, selectedCategory, description)
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
