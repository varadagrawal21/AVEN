package com.lifetracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lifetracker.ui.components.SectionHeader
import com.lifetracker.ui.components.StatCard
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.ui.theme.*
import com.lifetracker.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LifeTracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
                SectionHeader(title = "Today's Overview")
            }

            // Hydration card
            item {
                StatCard(
                    title = "💧 Hydration",
                    value = "${state.todayWaterMl} ml",
                    subtitle = "Goal: ${state.waterGoalMl} ml",
                    icon = Icons.Default.WaterDrop,
                    accentColor = HydrationBlue,
                    onClick = { navController.navigate(Screen.Hydration.route) }
                )
            }

            // Health card
            item {
                StatCard(
                    title = "💪 Health & Fitness",
                    value = "${state.todayCalories} kcal",
                    subtitle = "Goal: ${state.calorieGoal} kcal",
                    icon = Icons.Default.FitnessCenter,
                    accentColor = HealthGreen,
                    onClick = { navController.navigate(Screen.Health.route) }
                )
            }

            // Finance card
            item {
                StatCard(
                    title = "💰 Finance",
                    value = "₹${String.format("%.0f", state.monthBalance)}",
                    subtitle = "This month's balance",
                    icon = Icons.Default.AccountBalance,
                    accentColor = FinanceGold,
                    onClick = { navController.navigate(Screen.Finance.route) }
                )
            }

            // Vocabulary card
            item {
                StatCard(
                    title = "📖 Vocabulary",
                    value = "${state.wordCount} words",
                    subtitle = "Total words learned",
                    icon = Icons.Default.MenuBook,
                    accentColor = VocabPurple,
                    onClick = { navController.navigate(Screen.Vocabulary.route) }
                )
            }

            // Academic card
            item {
                StatCard(
                    title = "🎓 Academic",
                    value = "${state.todayStudyMinutes} min",
                    subtitle = "Study time today",
                    icon = Icons.Default.School,
                    accentColor = AcademicOrange,
                    onClick = { navController.navigate(Screen.Health.route) }
                )
            }

            // Books card
            item {
                StatCard(
                    title = "📚 Books",
                    value = "${state.completedBooks} completed",
                    subtitle = "Books finished",
                    icon = Icons.Default.LibraryBooks,
                    accentColor = BooksIndigo,
                    onClick = { navController.navigate(Screen.Books.route) }
                )
            }

            // Quotes card
            item {
                StatCard(
                    title = "💬 Quotes",
                    value = "${state.quoteCount} saved",
                    subtitle = "Quotes & principles",
                    icon = Icons.Default.FormatQuote,
                    accentColor = QuotesTeal,
                    onClick = { navController.navigate(Screen.Quotes.route) }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
