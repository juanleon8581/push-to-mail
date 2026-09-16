package com.jpleon.pushtomail.notification

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService

/**
 * Tras un reinicio, el sistema deberia rebindear solo los listeners ya
 * habilitados por el usuario, pero varios fabricantes (MIUI, EMUI, One UI)
 * retrasan o directamente saltean ese rebind. requestRebind() es la API
 * oficial para forzarlo -- no hace nada si el listener no fue habilitado.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        NotificationListenerService.requestRebind(
            ComponentName(context, NotificationCaptureService::class.java)
        )
    }
}
