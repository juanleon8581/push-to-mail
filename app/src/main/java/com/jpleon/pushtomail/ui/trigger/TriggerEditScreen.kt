package com.jpleon.pushtomail.ui.trigger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jpleon.pushtomail.data.Trigger
import com.jpleon.pushtomail.data.TriggerRepository
import com.jpleon.pushtomail.ui.apppicker.AppPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerEditScreen(
    repository: TriggerRepository,
    triggerId: Long,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableStateOf(triggerId == 0L) }

    var name by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(true) }
    var watchedPackages by remember { mutableStateOf(setOf<String>()) }
    var smtpHost by remember { mutableStateOf("") }
    var smtpPort by remember { mutableStateOf("587") }
    var smtpUser by remember { mutableStateOf("") }
    var smtpPassword by remember { mutableStateOf("") }
    var useTls by remember { mutableStateOf(true) }
    var fromEmail by remember { mutableStateOf("") }
    var toEmail by remember { mutableStateOf("") }
    var subjectTemplate by remember { mutableStateOf("Nueva notificacion de {{appName}}") }
    var bodyTemplate by remember {
        mutableStateOf("App: {{appName}}\nTitulo: {{title}}\nTexto: {{text}}\nHora: {{timestamp}}")
    }

    var showAppPicker by remember { mutableStateOf(false) }

    LaunchedEffect(triggerId) {
        if (triggerId != 0L) {
            repository.getById(triggerId)?.let { t ->
                name = t.name
                enabled = t.enabled
                watchedPackages = t.watchedPackages.toSet()
                smtpHost = t.smtpHost
                smtpPort = t.smtpPort.toString()
                smtpUser = t.smtpUser
                smtpPassword = t.smtpPassword
                useTls = t.useTls
                fromEmail = t.fromEmail
                toEmail = t.toEmail
                subjectTemplate = t.subjectTemplate
                bodyTemplate = t.bodyTemplate
            }
            loaded = true
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            initiallySelected = watchedPackages,
            onDismiss = { showAppPicker = false },
            onConfirm = { selected ->
                watchedPackages = selected
                showAppPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (triggerId == 0L) "Nuevo trigger" else "Editar trigger") },
                actions = {
                    if (triggerId != 0L) {
                        TextButton(onClick = {
                            scope.launch {
                                repository.deleteById(triggerId)
                                onDone()
                            }
                        }) { Text("Eliminar") }
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded) return@Scaffold

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Activo")
                Spacer(Modifier.width(8.dp))
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            OutlinedButton(onClick = { showAppPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Apps a escuchar (${watchedPackages.size})")
            }

            HorizontalDivider()

            OutlinedTextField(value = smtpHost, onValueChange = { smtpHost = it }, label = { Text("Host SMTP") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = smtpPort, onValueChange = { smtpPort = it }, label = { Text("Puerto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = smtpUser, onValueChange = { smtpUser = it }, label = { Text("Usuario SMTP") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = smtpPassword, onValueChange = { smtpPassword = it },
                label = { Text("Password / App Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("TLS")
                Spacer(Modifier.width(8.dp))
                Switch(checked = useTls, onCheckedChange = { useTls = it })
            }

            HorizontalDivider()

            OutlinedTextField(value = fromEmail, onValueChange = { fromEmail = it }, label = { Text("Email remitente") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = toEmail, onValueChange = { toEmail = it }, label = { Text("Email destino") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = subjectTemplate, onValueChange = { subjectTemplate = it }, label = { Text("Plantilla asunto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = bodyTemplate, onValueChange = { bodyTemplate = it },
                label = { Text("Plantilla cuerpo") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
            )

            Button(
                onClick = {
                    scope.launch {
                        repository.save(
                            Trigger(
                                id = triggerId,
                                name = name,
                                enabled = enabled,
                                watchedPackages = watchedPackages.toList(),
                                smtpHost = smtpHost,
                                smtpPort = smtpPort.toIntOrNull() ?: 587,
                                smtpUser = smtpUser,
                                smtpPassword = smtpPassword,
                                useTls = useTls,
                                fromEmail = fromEmail,
                                toEmail = toEmail,
                                subjectTemplate = subjectTemplate,
                                bodyTemplate = bodyTemplate
                            )
                        )
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}
