package com.jpleon.pushtomail.ui.trigger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.jpleon.pushtomail.data.Trigger
import com.jpleon.pushtomail.data.TriggerRepository
import com.jpleon.pushtomail.mail.SmtpMailSender
import com.jpleon.pushtomail.ui.apppicker.AppPickerDialog
import com.jpleon.pushtomail.ui.apppicker.InstalledApp
import com.jpleon.pushtomail.ui.apppicker.loadInstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerEditScreen(
    repository: TriggerRepository,
    triggerId: Long,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var loaded by remember { mutableStateOf(triggerId == 0L) }

    var name by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(true) }
    var watchedPackages by remember { mutableStateOf(setOf<String>()) }
    var smtpHost by remember { mutableStateOf("") }
    var smtpPort by remember { mutableStateOf("587") }
    var smtpUser by remember { mutableStateOf("") }
    var smtpPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var useTls by remember { mutableStateOf(true) }
    var fromEmail by remember { mutableStateOf("") }
    var toEmail by remember { mutableStateOf("") }
    var subjectTemplate by remember { mutableStateOf("Nueva notificacion de {{appName}}") }
    var bodyTemplate by remember {
        mutableStateOf("App: {{appName}}\nTitulo: {{title}}\nTexto: {{text}}\nHora: {{timestamp}}")
    }

    var installedApps by remember { mutableStateOf<List<InstalledApp>?>(null) }
    var loadingApps by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var testSending by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Boolean?>(null) }
    var testResultMessage by remember { mutableStateOf("") }

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
        installedApps?.let { apps ->
            AppPickerDialog(
                apps = apps,
                initiallySelected = watchedPackages,
                onDismiss = { showAppPicker = false },
                onConfirm = { selected ->
                    watchedPackages = selected
                    showAppPicker = false
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (triggerId == 0L) "Nuevo trigger" else "Editar trigger") },
                actions = {
                    if (triggerId != 0L) {
                        IconButton(onClick = {
                            scope.launch {
                                repository.deleteById(triggerId)
                                onDone()
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar trigger")
                        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Activo")
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }

                OutlinedButton(
                    onClick = {
                        val cached = installedApps
                        if (cached != null) {
                            showAppPicker = true
                        } else {
                            loadingApps = true
                            scope.launch {
                                val apps = withContext(Dispatchers.IO) { loadInstalledApps(context) }
                                installedApps = apps
                                loadingApps = false
                                showAppPicker = true
                            }
                        }
                    },
                    enabled = !loadingApps,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (loadingApps) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp).size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Apps, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    }
                    Text(if (loadingApps) "Cargando apps..." else "Apps a escuchar (${watchedPackages.size})")
                }
            }

            SectionCard {
                Text("Configuracion SMTP", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = smtpHost, onValueChange = { smtpHost = it },
                    label = { Text("Host SMTP") },
                    leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = smtpPort, onValueChange = { smtpPort = it },
                    label = { Text("Puerto") },
                    leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = smtpUser, onValueChange = { smtpUser = it },
                    label = { Text("Usuario SMTP") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = smtpPassword, onValueChange = { smtpPassword = it },
                    label = { Text("App Password") },
                    supportingText = { Text("Solo App Password. No uses la contraseña principal de tu cuenta.") },
                    leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar password" else "Mostrar password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("TLS")
                    }
                    Switch(checked = useTls, onCheckedChange = { useTls = it })
                }
            }

            SectionCard {
                Text("Destino y prueba", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = fromEmail, onValueChange = { fromEmail = it },
                    label = { Text("Email remitente") },
                    leadingIcon = { Icon(Icons.Filled.Send, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = toEmail, onValueChange = { toEmail = it },
                    label = { Text("Email destino") },
                    leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                val canTest = smtpHost.isNotBlank() && smtpUser.isNotBlank() && smtpPassword.isNotBlank() &&
                    fromEmail.isNotBlank() && toEmail.isNotBlank() && !testSending

                OutlinedButton(
                    onClick = {
                        testSending = true
                        testResult = null
                        scope.launch {
                            val port = smtpPort.toIntOrNull() ?: 587
                            val outcome = try {
                                withContext(Dispatchers.IO) {
                                    SmtpMailSender().send(
                                        host = smtpHost,
                                        port = port,
                                        user = smtpUser,
                                        password = smtpPassword,
                                        useTls = useTls,
                                        from = fromEmail,
                                        to = toEmail,
                                        subject = "Correo de prueba - push-to-mail",
                                        body = "Este es un correo de prueba de configuracion SMTP de push-to-mail."
                                    )
                                }
                                true to "Correo de prueba enviado."
                            } catch (e: Exception) {
                                false to "Fallo el envio: ${e.message ?: e.javaClass.simpleName}"
                            }
                            testResult = outcome.first
                            testResultMessage = outcome.second
                            testSending = false
                        }
                    },
                    enabled = canTest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (testSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp).size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    }
                    Text(if (testSending) "Enviando..." else "Probar envio")
                }

                testResult?.let { success ->
                    Text(
                        text = testResultMessage,
                        color = if (success) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                }
            }

            SectionCard {
                Text("Plantillas", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = subjectTemplate, onValueChange = { subjectTemplate = it },
                    label = { Text("Plantilla asunto") },
                    leadingIcon = { Icon(Icons.Filled.Subject, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bodyTemplate, onValueChange = { bodyTemplate = it },
                    label = { Text("Plantilla cuerpo") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
                )
            }

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
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}
