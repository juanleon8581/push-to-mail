package com.jpleon.pushtomail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jpleon.pushtomail.ui.settings.PermissionsScreen
import com.jpleon.pushtomail.ui.theme.PushToMailTheme
import com.jpleon.pushtomail.ui.trigger.TriggerEditScreen
import com.jpleon.pushtomail.ui.trigger.TriggerListScreen

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val repository = (application as PushToMailApp).serviceLocator.triggerRepository

        setContent {
            PushToMailTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "list") {
                        composable("list") {
                            TriggerListScreen(
                                repository = repository,
                                onAddTrigger = { navController.navigate("edit/0") },
                                onEditTrigger = { id -> navController.navigate("edit/$id") },
                                onOpenPermissions = { navController.navigate("permissions") }
                            )
                        }
                        composable("permissions") {
                            PermissionsScreen()
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
