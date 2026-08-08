package com.example.ade.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ade.R
import com.example.ade.ui.screens.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Input : Screen("input", "Saisie", Icons.Default.Edit)
    object History : Screen("history", "Historique", Icons.Default.List)
    object About : Screen("about", "À propos", Icons.Default.Info)
    object Tariffs : Screen("tariffs", "Tarifs", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingApp() {
    val navController = rememberNavController()
    val viewModel: BillingViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ade),
                            contentDescription = "ADE Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            when (currentRoute) {
                                "input" -> "Simulation ADE"
                                "history" -> "Historique"
                                "result" -> "Détail Facture"
                                "about" -> "À propos"
                                "tariffs" -> "Barème Officiel"
                                else -> "ADE Relevé"
                            }
                        )
                    }
                },
                navigationIcon = {
                    if (currentRoute == "result" || currentRoute == "about" || currentRoute == "tariffs") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    }
                },
                actions = {
                    if (currentRoute != "about") {
                        IconButton(onClick = { navController.navigate(Screen.About.route) }) {
                            Icon(Icons.Default.Info, contentDescription = "À propos")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (currentRoute == "input" || currentRoute == "history") {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    val items = listOf(Screen.Input, Screen.History)
                    
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
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
            navController, 
            startDestination = Screen.Input.route, 
            Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
            exitTransition = { fadeOut(tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
            popEnterTransition = { fadeIn(tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) },
            popExitTransition = { fadeOut(tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) }
        ) {
            composable(Screen.Input.route) { InputScreen(navController, viewModel) }
            composable(Screen.History.route) { HistoryScreen(viewModel) }
            composable("result") { ResultScreen(navController, viewModel) }
            composable(Screen.About.route) { AboutScreen(navController) }
            composable(Screen.Tariffs.route) { TariffScreen(navController) }
        }
    }
}

