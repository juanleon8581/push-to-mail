# Push to Mail

Native Android app (Kotlin + Jetpack Compose) that listens to system notifications in the background and, when a notification from an app you chose arrives, builds an email from a template and sends it via SMTP to a fixed recipient. Built for cases like "email me when a bank transfer notification comes in."

## How it works

You define one or more **triggers**. Each trigger is a complete, independent instance with:

- a list of apps to watch (picked from the apps installed on the device),
- its own SMTP configuration (host, port, user, App Password, TLS),
- its own sender/recipient,
- its own subject/body templates (with placeholders `{{appName}}`, `{{title}}`, `{{text}}`, `{{timestamp}}`),
- its own on/off switch.

There's no limit on the number of triggers, and no config is shared between them.

When a notification arrives from an app watched by an active trigger, the email is built from the template and the send is queued. WorkManager handles retries if it fails (no connectivity at that moment, for example).

## Features

- **Test send**: verify a trigger's SMTP configuration without waiting for a real notification.
- **App Password only**: the password field is exclusively for an App Password from your mail provider (Gmail, etc.), never your account's main password.
- **Searchable app picker**: choose which installed apps trigger each rule, with a search box and real app icons.
- **Duplicate trigger**: clone an existing trigger (including its SMTP config) to quickly build variants.
- **Result push notification**: a system notification telling you whether the email send succeeded or failed (after retries are exhausted).
- **Permissions screen**: a central panel that live-checks notification access, battery optimization exclusion, and the push notification permission, with a direct link to each setting.
- **Background reliability**: designed to keep working while the app is closed — the notification listener is bound by the system, it re-binds automatically on device boot, and the app guides you through excluding it from battery optimization (needed on manufacturers like Xiaomi/Samsung/Huawei that restrict background execution beyond stock Android).
- **Empty notification filtering**: if a notification arrives with no title and no text (a placeholder some apps post before the real content), no email is sent.
- **Fixed dark theme**, with Material Icons and a card grid for managing triggers.

## Architecture

No DI framework (no Hilt, no Koin): `di/ServiceLocator.kt` is a manual container hung off the `Application`. There's no ViewModel layer either — Compose screens call `TriggerRepository` directly (a deliberate simplification).

**Data model split across two storages, on purpose:**

- `data/local/` (Room): each trigger's configuration — name, watched apps, SMTP data except the password, templates.
- `data/CredentialStore.kt` (`EncryptedSharedPreferences`, AES-256-GCM): only the SMTP password for each trigger, never stored in Room.
- `data/TriggerRepository.kt` merges both storages into a single domain model so the rest of the app doesn't need to know about the split.

**Capture-to-send flow:**

1. `notification/NotificationCaptureService.kt` (a `NotificationListenerService`, requires a manual grant from the user) detects every posted notification and checks which active triggers are watching it.
2. On each match, it enqueues a `mail/MailWorker.kt` (WorkManager) with the notification's data.
3. `MailWorker` renders the template via `mail/EmailTemplateEngine.kt` and sends it over SMTP with `mail/SmtpMailSender.kt` (JavaMail, blocking — runs inside the Worker, never on the main thread).
4. `mail/MailResultNotifier.kt` notifies the user of the final outcome (success, or failure after retries are exhausted).

## Requirements and permissions

The app needs the user to manually grant (the in-app Permissions screen walks through each one):

- **Notification access** (`NotificationListenerService`): without this, no trigger ever fires.
- **Battery optimization exclusion**: so the system doesn't kill the process in the background.
- **Push notifications** (Android 13+): for the send-result alert.

On manufacturers with their own battery management (MIUI, One UI, EMUI), you may also need to manually enable "autostart" / "no restrictions" for the app from the manufacturer's Security app — there's no public Android API for that.

## Building

The Gradle wrapper (`gradlew`) is not committed. Before building:

- Open the project in Android Studio once (it regenerates the wrapper automatically), or
- Run `gradle wrapper --gradle-version 8.7` if Gradle is installed locally.

With the wrapper in place:

```bash
./gradlew assembleDebug              # build debug APK
./gradlew installDebug                # build and install on a connected device/emulator
./gradlew test                        # unit tests
./gradlew connectedAndroidTest        # instrumented tests (requires a connected device/emulator)
```

## Stack

Kotlin · Jetpack Compose (Material 3) · Room · WorkManager · EncryptedSharedPreferences · JavaMail (`com.sun.mail:android-mail`) · Navigation-Compose.

minSdk 26, targetSdk/compileSdk 34.
