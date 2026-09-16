package com.jpleon.pushtomail.ui.trigger

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.core.app.NotificationManagerCompat
import com.jpleon.pushtomail.data.TriggerRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerListScreen(
    repository: TriggerRepository,
    onAddTrigger: () -> Unit,
    onEditTrigger: (Long) -> Unit
) {
    val triggers by repository.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listenerEnabled = remember {
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Push to Mail") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTrigger) { Text("+") }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!listenerEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).clickable {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                ) {
                    Text(
                        "Falta habilitar acceso a notificaciones. Tocar para abrir ajustes.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(triggers, key = { it.id }) { trigger ->
                    ListItem(
                        headlineContent = { Text(trigger.name) },
                        supportingContent = { Text("${trigger.watchedPackages.size} apps -> ${trigger.toEmail}") },
                        trailingContent = {
                            Switch(
                                checked = trigger.enabled,
                                onCheckedChange = { checked ->
                                    scope.launch { repository.setEnabled(trigger.id, checked) }
                                }
                            )
                        },
                        modifier = Modifier.clickable { onEditTrigger(trigger.id) }
                    )
                }
            }
        }
    }
}
