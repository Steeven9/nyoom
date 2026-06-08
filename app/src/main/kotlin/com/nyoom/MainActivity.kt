package com.nyoom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nyoom.ui.LocalLocationTracker
import com.nyoom.ui.LocalTripRepository
import com.nyoom.ui.about.AboutScreen
import com.nyoom.ui.diary.DiaryScreen
import com.nyoom.ui.map.MapScreen
import com.nyoom.ui.riding.RidingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = applicationContext as NyoomApp
            val repository = app.getRepository()
            val locationTracker = app.locationTracker

            androidx.compose.runtime.CompositionLocalProvider(
                LocalTripRepository provides repository,
                LocalLocationTracker provides locationTracker,
            ) {
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute?.startsWith("map") != true

                Scaffold(
                    bottomBar = { if (showBottomBar) BottomNavBar(navController) }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "riding",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("riding") { RidingScreen() }
                        composable("diary") { DiaryScreen(navController) }
                        composable("about") { AboutScreen() }
                        composable(
                            "map/{tripId}",
                            arguments = listOf(
                                navArgument("tripId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getInt("tripId") ?: 0
                            MapScreen(tripId = tripId, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "riding",
            onClick = { navController.navigate("riding") },
            label = { Text("Riding") },
            icon = { Icon(painterResource(android.R.drawable.ic_menu_compass), "Riding") }
        )
        NavigationBarItem(
            selected = currentRoute == "diary",
            onClick = { navController.navigate("diary") },
            label = { Text("Diary") },
            icon = { Icon(painterResource(android.R.drawable.ic_menu_info_details), "Diary") }
        )
        NavigationBarItem(
            selected = currentRoute == "about",
            onClick = { navController.navigate("about") },
            label = { Text("About") },
            icon = { Icon(painterResource(android.R.drawable.ic_menu_help), "About") }
        )
    }
}
