package com.example.fastnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.fastnews.ui.theme.FastNewsTheme

// Helper data class for bottom navigation items
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any // Using Any for type-safe routes
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // The ViewModelFactory now manages the repository, so we pass the application context.
        val viewModelFactory = ViewModelFactory(application)
        val newsViewModel = ViewModelProvider(this, viewModelFactory)[NewsViewModel::class.java]
        val settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        setContent {
            val darkMode by settingsViewModel.darkModeEnabled.observeAsState(isSystemInDarkTheme())

            FastNewsTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomNavItems = listOf(
                    BottomNavItem(
                        label = "Home",
                        icon = Icons.Filled.Home,
                        route = HomePageScreen
                    ),
                    BottomNavItem(
                        label = "Bookmarks",
                        icon = Icons.Filled.Bookmarks,
                        route = BookmarksScreen
                    ),
                    BottomNavItem(
                        label = "Settings",
                        icon = Icons.Filled.Settings,
                        route = SettingsScreen
                    )
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val shouldShowBottomNav = currentDestination?.route == HomePageScreen::class.qualifiedName ||
                                currentDestination?.route == BookmarksScreen::class.qualifiedName ||
                                currentDestination?.route == SettingsScreen::class.qualifiedName

                        if (shouldShowBottomNav) {
                            NavigationBar {
                                bottomNavItems.forEach { item ->
                                    NavigationBarItem(
                                        selected = currentDestination?.route == item.route::class.qualifiedName,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = HomePageScreen,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<HomePageScreen> {
                            HomePage(newsViewModel, settingsViewModel, navController)
                        }
                        composable<NewsArticleScreen> {
                            val args = it.toRoute<NewsArticleScreen>()
                            NewsArticlePage(args.url)
                        }
                        composable<BookmarksScreen> {
                            BookmarksScreen(settingsViewModel, navController)
                        }
                        composable<SettingsScreen> {
                            SettingsScreen(settingsViewModel)
                        }
                    }
                }
            }
        }
    }
}
