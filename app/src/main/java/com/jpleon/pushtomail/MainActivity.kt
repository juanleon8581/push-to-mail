package com.jpleon.pushtomail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jpleon.pushtomail.ui.trigger.TriggerEditScreen
import com.jpleon.pushtomail.ui.trigger.TriggerListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as PushToMailApp).serviceLocator.triggerRepository

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "list") {
                        composable("list") {
                            TriggerListScreen(
                                repository = repository,
                                onAddTrigger = { navController.navigate("edit/0") },
                                onEditTrigger = { id -> navController.navigate("edit/$id") }
                            )
                        }
                        composable("edit/{triggerId}") { backStackEntry ->
                            val triggerId = backStackEntry.arguments
                                ?.getString("triggerId")?.toLongOrNull() ?: 0L
                            TriggerEditScreen(
                                repository = repository,
                                triggerId = triggerId,
                                onDone = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
