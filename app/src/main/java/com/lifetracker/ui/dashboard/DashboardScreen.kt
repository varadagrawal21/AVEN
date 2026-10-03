package com.lifetracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lifetracker.ui.components.*
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.ui.theme.*
import com.lifetracker.viewmodel.DashboardViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    
    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = "LifeTracker",
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = WhiteHigh
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = GlassElevation.Thin
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = today.format(dateFormatter),
                            style = MaterialTheme.typography.labelMedium,
                            color = WhiteMedium
                        )
                        Text(
                            text = "Welcome back",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WhiteHigh
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            val stats = listOf(
                StatItem("Hydration", "${state.todayWaterMl}", "ml", Icons.Default.WaterDrop, HydrationBlue, Screen.Hydration.route),
                StatItem("Health", "${state.todayCalories}", "kcal", WeatherIconsDefault.FitnessCenter, HealthGreen, Screen.Health.route),
                StatItem("Finance", "${String.format("%.0f", state.monthBalance)}", "balance", Icons.Default.AccountBalance, FinanceGold, Screen.Finance.route),
                StatItem("Vocabulary", "${state.wordCount}", "words", Icons.Default.MenuBook, VocabPurple, Screen.Vocabulary.route),
                StatItem("Academic", "${state.todayStudyMinutes}", "min", Icons.Default.School, AcademicOrange, Screen.Academic.route),
                StatItem("Books", "${state.completedBooks}", "done", Icons.Default.LibraryBooks, BooksIndigo, Screen.Books.route),
                StatItem("Quotes", "${state.quoteCount}", "saved", Icons.Default.FormatQuote, QuotesTeal, Screen.Quotes.route),
                StatItem("People", "", "tracker", Icons.Default.People, PeoplePink, Screen.People.route)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(stats) { stat ->
                    DashboardStatCard(
                        title = stat.title,
                        value = stat.value,
                        subtitle = stat.subtitle,
                        icon = stat.icon,
                        accentColor = stat.color,
                        onClick = { navController.navigate(stat.route) }
                    )
                }
                
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = GlassElevation.Thin
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = WhiteHigh
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuickActionButton(
                                    icon = Icons.Default.Add,
                                    label = "Log Water",
                                    color = HydrationBlue,
                                    modifier = Modifier.weight(1f),
                                    onClick = { navController.navigate(Screen.Hydration.route) }
                                )
                                QuickActionButton(
                                    icon = Icons.Default.MenuBook,
                                    label = "Add Word",
                                    color = VocabPurple,
                                    modifier = Modifier.weight(1f),
                                    onClick = { navController.navigate(Screen.Vocabulary.route) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class StatItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        elevation = GlassElevation.Thin,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = WhiteMedium
            )
        }
    }
}

private object WeatherIconsDefault {
    val FitnessCenter: ImageVector = Icons.Default.FitnessCenter
}
