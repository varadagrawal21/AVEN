package com.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lifetracker.ui.academic.AcademicScreen
import com.lifetracker.ui.books.BookDetailScreen
import com.lifetracker.ui.books.BooksScreen
import com.lifetracker.ui.books.PdfViewerScreen
import com.lifetracker.ui.custom.CustomScreen
import com.lifetracker.ui.custom.CustomSectionDetailScreen
import com.lifetracker.ui.dashboard.DashboardScreen
import com.lifetracker.ui.finance.FinanceScreen
import com.lifetracker.ui.health.HealthScreen
import com.lifetracker.ui.hydration.HydrationScreen
import com.lifetracker.ui.navigation.Screen
import com.lifetracker.ui.navigation.bottomNavItems
import com.lifetracker.ui.people.PeopleScreen
import com.lifetracker.ui.people.PersonDetailScreen
import com.lifetracker.ui.quotes.QuoteDetailScreen
import com.lifetracker.ui.quotes.QuotesScreen
import com.lifetracker.ui.settings.SettingsScreen
import com.lifetracker.ui.theme.LifeTrackerTheme
import com.lifetracker.ui.vocabulary.VocabDetailScreen
import com.lifetracker.ui.vocabulary.VocabularyScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeTrackerTheme {
                LifeTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeTrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Screens that show the bottom nav bar
    val topLevelRoutes = bottomNavItems.map { it.screen.route }
    val showBottomBar = currentDestination?.route in topLevelRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Top-level destinations
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }
            composable(Screen.Hydration.route) {
                HydrationScreen()
            }
            composable(Screen.Health.route) {
                HealthScreen()
            }
            composable(Screen.Finance.route) {
                FinanceScreen()
            }
            composable(Screen.Vocabulary.route) {
                VocabularyScreen(navController = navController)
            }
            composable(Screen.Books.route) {
                BooksScreen(navController = navController)
            }
            composable(Screen.Quotes.route) {
                QuotesScreen(navController = navController)
            }
            composable(Screen.People.route) {
                PeopleScreen(navController = navController)
            }
            composable(Screen.Custom.route) {
                CustomScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // Detail screens
            composable(
                route = Screen.BookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                BookDetailScreen(bookId = bookId, navController = navController)
            }
            composable(
                route = Screen.PdfViewer.route,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                PdfViewerScreen(bookId = bookId, navController = navController)
            }
            composable(
                route = Screen.PersonDetail.route,
                arguments = listOf(navArgument("personId") { type = NavType.LongType })
            ) { backStackEntry ->
                val personId = backStackEntry.arguments?.getLong("personId") ?: 0L
                PersonDetailScreen(personId = personId, navController = navController)
            }
            composable(
                route = Screen.QuoteDetail.route,
                arguments = listOf(navArgument("quoteId") { type = NavType.LongType })
            ) { backStackEntry ->
                val quoteId = backStackEntry.arguments?.getLong("quoteId") ?: 0L
                QuoteDetailScreen(quoteId = quoteId, navController = navController)
            }
            composable(
                route = Screen.VocabDetail.route,
                arguments = listOf(navArgument("wordId") { type = NavType.LongType })
            ) { backStackEntry ->
                val wordId = backStackEntry.arguments?.getLong("wordId") ?: 0L
                VocabDetailScreen(wordId = wordId, navController = navController)
            }
            composable(
                route = Screen.CustomSectionDetail.route,
                arguments = listOf(navArgument("sectionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sectionId = backStackEntry.arguments?.getLong("sectionId") ?: 0L
                CustomSectionDetailScreen(sectionId = sectionId, navController = navController)
            }
        }
    }
}
