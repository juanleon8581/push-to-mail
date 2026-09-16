package com.jpleon.pushtomail.mail

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jpleon.pushtomail.PushToMailApp

/**
 * One enqueue per (trigger, notification) match. WorkManager gives us retry-on-failure
 * for free, which matters here since SMTP over mobile data flakes.
 */
class MailWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as PushToMailApp
        val triggerId = inputData.getLong(KEY_TRIGGER_ID, -1L)
        if (triggerId == -1L) return Result.failure()

        val trigger = app.serviceLocator.triggerRepository.getById(triggerId)
            ?: return Result.failure()

        val appName = inputData.getString(KEY_APP_NAME).orEmpty()
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val text = inputData.getString(KEY_TEXT).orEmpty()

        val subject = EmailTemplateEngine.render(trigger.subjectTemplate, appName, title, text)
        val body = EmailTemplateEngine.render(trigger.bodyTemplate, appName, title, text)

        return try {
            SmtpMailSender().send(
                host = trigger.smtpHost,
                port = trigger.smtpPort,
                user = trigger.smtpUser,
                password = trigger.smtpPassword,
                useTls = trigger.useTls,
                from = trigger.fromEmail,
                to = trigger.toEmail,
                subject = subject,
                body = body
            )
            MailResultNotifier.notifyResult(applicationContext, trigger.id, trigger.name, success = true)
            Result.success()
        } catch (e: Exception) {
            // Solo avisar al usuario cuando el proceso termina de verdad -- reintentos
            // silenciosos hasta agotar MAX_ATTEMPTS, recien ahi se notifica el fallo final.
            if (runAttemptCount + 1 >= MAX_ATTEMPTS) {
                MailResultNotifier.notifyResult(applicationContext, trigger.id, trigger.name, success = false)
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        const val KEY_TRIGGER_ID = "trigger_id"
        const val KEY_APP_NAME = "app_name"
        const val KEY_TITLE = "title"
        const val KEY_TEXT = "text"
        private const val MAX_ATTEMPTS = 3
    }
}
