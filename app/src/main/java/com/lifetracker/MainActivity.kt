package com.lifetracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.lifetracker.ui.theme.*
import com.lifetracker.ui.vocabulary.VocabDetailScreen
import com.lifetracker.ui.vocabulary.VocabularyScreen
import com.lifetracker.ui.components.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var notificationDestination by mutableStateOf<String?>(null)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationDestination = intent?.getStringExtra("navigate_to")
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            LifeTrackerTheme {
                LifeTrackerAppContent(initialDestination = notificationDestination)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notificationDestination = intent.getStringExtra("navigate_to")
    }
}

@Composable
fun LifeTrackerAppContent(initialDestination: String? = null) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(initialDestination) {
        val route = when (initialDestination) {
            "hydration" -> Screen.Hydration.route
            "health" -> Screen.Health.route
            "finance" -> Screen.Finance.route
            "academic" -> Screen.Academic.route
            "vocabulary" -> Screen.Vocabulary.route
            "books" -> Screen.Books.route
            "quotes" -> Screen.Quotes.route
            "people" -> Screen.People.route
            "custom" -> Screen.Custom.route
            "settings" -> Screen.Settings.route
            else -> null
        }
        if (route != null) {
            navController.navigate(route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val topLevelRoutes = bottomNavItems.map { it.screen.route }
    val showBottomBar = currentDestination?.route in topLevelRoutes
    
    val navItems = bottomNavItems.mapIndexed { index, item ->
        NavItem(
            route = item.screen.route,
            label = item.label,
            selectedIcon = item.selectedIcon,
            unselectedIcon = item.unselectedIcon,
            accentColor = when (item.screen) {
                Screen.Dashboard -> AccentBlue
                Screen.Hydration -> HydrationBlue
                Screen.Health -> HealthGreen
                Screen.Finance -> FinanceGold
                Screen.Academic -> AcademicOrange
                Screen.Vocabulary -> VocabPurple
                Screen.Books -> BooksIndigo
                Screen.Quotes -> QuotesTeal
                Screen.People -> PeoplePink
                Screen.Custom -> CustomGray
                Screen.Settings -> SettingsBlue
                else -> AccentBlue
            }
        )
    }

    val selectedNavIndex = navItems.indexOfFirst { it.route == currentDestination?.route }
        .takeIf { it >= 0 } ?: 0

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                GlassScrollingNavigationBar(
                    items = navItems,
                    selectedItemIndex = selectedNavIndex,
                    onItemSelected = { index ->
                        val route = navItems[index].route
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = BlackPure
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
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
            composable(Screen.Academic.route) {
                AcademicScreen()
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
