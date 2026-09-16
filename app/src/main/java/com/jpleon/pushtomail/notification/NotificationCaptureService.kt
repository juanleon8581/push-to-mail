package com.jpleon.pushtomail.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jpleon.pushtomail.PushToMailApp
import com.jpleon.pushtomail.mail.MailWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * System-wide notification listener (requires manual grant via
 * Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS -- surfaced in TriggerListScreen).
 * Every posted notification is checked against every enabled trigger's watched-package
 * list; each match enqueues its own MailWorker.
 */
class NotificationCaptureService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()

        scope.launch {
            val app = applicationContext as PushToMailApp
            val matches = app.serviceLocator.triggerRepository.getEnabledForPackage(packageName)
            matches.forEach { trigger -> enqueueMail(trigger.id, packageName, title, text) }
        }
    }

    private fun enqueueMail(triggerId: Long, appName: String, title: String, text: String) {
        val data = Data.Builder()
            .putLong(MailWorker.KEY_TRIGGER_ID, triggerId)
            .putString(MailWorker.KEY_APP_NAME, appName)
            .putString(MailWorker.KEY_TITLE, title)
            .putString(MailWorker.KEY_TEXT, text)
            .build()

        val request = OneTimeWorkRequestBuilder<MailWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
    }
}
