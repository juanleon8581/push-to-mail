# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Qué es

App Android nativa (Kotlin + Jetpack Compose) que corre en background escuchando notificaciones del sistema. El usuario define "triggers": cada uno filtra por una lista de apps, y cuando una notificación de esas apps llega, arma un email con una plantilla y lo manda por SMTP a un destinatario fijo. Cada trigger tiene su propio switch on/off y su propia config SMTP — son instancias independientes, no una config global compartida.

Scaffold generado sin Android SDK/Gradle instalados en esta sesión (`gradle`, `sdkmanager` y `ANDROID_HOME` no disponibles) — el código nunca se compiló ni se corrió. Antes de confiar en cualquier cambio, compilar y revisar errores de verdad.

## Comandos

El wrapper de Gradle (`gradlew`, `gradle-wrapper.jar`) no está commiteado — no había Gradle instalado para generarlo. Primer paso obligatorio en cualquier sesión nueva:

- Abrir el proyecto en Android Studio una vez (regenera el wrapper automáticamente), o
- Correr `gradle wrapper --gradle-version 8.7` si hay Gradle instalado localmente.

Con el wrapper ya generado:

```bash
./gradlew assembleDebug              # compilar APK debug
./gradlew installDebug                # compilar e instalar en dispositivo/emulador conectado
./gradlew test                        # unit tests (JVM, todos los módulos)
./gradlew testDebugUnitTest --tests "com.jpleon.pushtomail.SomeClassTest"   # un solo test
./gradlew connectedAndroidTest        # instrumented tests (requiere dispositivo/emulador)
./gradlew lint                        # Android lint
```

## Arquitectura

Sin DI framework (ni Hilt ni Koin) — `di/ServiceLocator.kt` es un contenedor manual colgado de `PushToMailApp` (la `Application`). Se accede como `(application as PushToMailApp).serviceLocator`. Tampoco hay capa de ViewModel: las pantallas Compose llaman directo al `TriggerRepository` (simplificación deliberada del scaffold inicial).

**Modelo de datos partido en dos storages, a propósito:**
- `data/local/` (Room, `AppDatabase` → tabla `triggers`): config de cada trigger — nombre, enabled, `watchedPackages` (lista de package names, convertida a CSV vía `Converters`), host/puerto/usuario SMTP, from/to, plantillas de asunto y cuerpo.
- `data/CredentialStore.kt` (`EncryptedSharedPreferences`, cifrado con `MasterKey` AES256-GCM): **solo** el password SMTP de cada trigger, keyed por `triggerId`. Nunca vive en Room ni en el modelo `TriggerEntity`.
- `data/TriggerRepository.kt` junta ambos storages en un solo modelo de dominio `Trigger` (con password incluido) para que el resto de la app no tenga que conocer el split. Al hacer `save()`/`deleteById()` mantiene ambos storages en sync.

**Flujo de captura → envío:**
1. `notification/NotificationCaptureService.kt` extiende `NotificationListenerService` (requiere grant manual del usuario vía Settings — `TriggerListScreen` muestra un banner si no está habilitado, usando `NotificationManagerCompat.getEnabledListenerPackages`). Por cada notificación posteada, busca en `TriggerRepository.getEnabledForPackage(packageName)` qué triggers activos la están escuchando.
2. Por cada trigger que matchea, encola un `MailWorker` (WorkManager `CoroutineWorker`) pasándole `triggerId` + datos de la notificación (`app_name`, `title`, `text`) vía `Data`. WorkManager da retry automático — importante porque el envío SMTP puede fallar en datos móviles.
3. `MailWorker` recarga el `Trigger` completo (con password) desde el repo, renderiza asunto/cuerpo con `mail/EmailTemplateEngine.kt` (placeholders `{{appName}}`, `{{title}}`, `{{text}}`, `{{timestamp}}`) y llama a `mail/SmtpMailSender.kt` (JavaMail vía `com.sun.mail:android-mail`, bloqueante — por eso corre dentro del Worker, nunca en el main thread).

**UI:** Compose + Navigation-Compose, dos pantallas (`ui/trigger/TriggerListScreen.kt`, `ui/trigger/TriggerEditScreen.kt`) más un diálogo (`ui/apppicker/AppPickerDialog.kt`) que lista apps instaladas vía `PackageManager.getInstalledApplications` filtradas a las que tienen launcher intent. El switch on/off de cada trigger vive directo en la lista, sin entrar a editar.

**Multi-instancia:** cada fila de la tabla `triggers` es una instancia independiente y completa (su propio filtro de apps, su propio SMTP, su propio switch). No hay límite de triggers ni config compartida entre ellos salvo la que el usuario copie a mano.
