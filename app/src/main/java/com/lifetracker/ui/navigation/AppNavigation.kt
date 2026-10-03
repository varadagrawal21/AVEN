package com.lifetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// ─────────────────────────────────────────────────────────────────────────────
// Screen routes
// ─────────────────────────────────────────────────────────────────────────────
sealed class Screen(val route: String) {
    // Bottom nav destinations
    object Dashboard : Screen("dashboard")
    object Hydration : Screen("hydration")
    object Health : Screen("health")
    object Finance : Screen("finance")
    object Academic : Screen("academic")
    object Vocabulary : Screen("vocabulary")
    object Books : Screen("books")
    object Quotes : Screen("quotes")
    object People : Screen("people")
    object Custom : Screen("custom")
    object Settings : Screen("settings")

    // Detail screens
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }
    object PersonDetail : Screen("person_detail/{personId}") {
        fun createRoute(personId: Long) = "person_detail/$personId"
    }
    object QuoteDetail : Screen("quote_detail/{quoteId}") {
        fun createRoute(quoteId: Long) = "quote_detail/$quoteId"
    }
    object VocabDetail : Screen("vocab_detail/{wordId}") {
        fun createRoute(wordId: Long) = "vocab_detail/$wordId"
    }
    object CustomSectionDetail : Screen("custom_section/{sectionId}") {
        fun createRoute(sectionId: Long) = "custom_section/$sectionId"
    }
    object PdfViewer : Screen("pdf_viewer/{bookId}") {
        fun createRoute(bookId: Long) = "pdf_viewer/$bookId"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Navigation Items
// ─────────────────────────────────────────────────────────────────────────────
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Dashboard,
        label = "Dashboard",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Hydration,
        label = "Hydration",
        selectedIcon = Icons.Filled.WaterDrop,
        unselectedIcon = Icons.Outlined.WaterDrop
    ),
    BottomNavItem(
        screen = Screen.Health,
        label = "Health",
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter
    ),
    BottomNavItem(
        screen = Screen.Finance,
        label = "Finance",
        selectedIcon = Icons.Filled.AccountBalance,
        unselectedIcon = Icons.Outlined.AccountBalance
    ),
    BottomNavItem(
        screen = Screen.Academic,
        label = "Academic",
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School
    ),
    BottomNavItem(
        screen = Screen.Vocabulary,
        label = "Vocab",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    ),
    BottomNavItem(
        screen = Screen.Books,
        label = "Books",
        selectedIcon = Icons.Filled.LibraryBooks,
        unselectedIcon = Icons.Outlined.LibraryBooks
    ),
    BottomNavItem(
        screen = Screen.Quotes,
        label = "Quotes",
        selectedIcon = Icons.Filled.FormatQuote,
        unselectedIcon = Icons.Outlined.FormatQuote
    ),
    BottomNavItem(
        screen = Screen.People,
        label = "People",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    ),
    BottomNavItem(
        screen = Screen.Custom,
        label = "Custom",
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView
    ),
    BottomNavItem(
        screen = Screen.Settings,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
