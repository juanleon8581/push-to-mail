package com.jpleon.pushtomail.mail

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Feedback al usuario del resultado final de un envio SMTP (exito o fallo
 * definitivo tras agotar reintentos) -- MailWorker es el unico caller.
 */
object MailResultNotifier {
    private const val CHANNEL_ID = "mail_result"
    private const val NOTIFICATION_ID_OFFSET = 100_000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Resultado de envio",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun notifyResult(context: Context, triggerId: Long, triggerName: String, success: Boolean) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle(if (success) "Correo enviado" else "Fallo el envio")
            .setContentText(
                if (success) "\"$triggerName\" envio el correo correctamente."
                else "\"$triggerName\" no pudo enviar el correo tras varios intentos."
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_OFFSET + triggerId.toInt(), notification)
    }
}
