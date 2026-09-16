package com.jpleon.pushtomail

import android.app.Application
import com.jpleon.pushtomail.di.ServiceLocator
import com.jpleon.pushtomail.mail.MailResultNotifier

class PushToMailApp : Application() {
    val serviceLocator by lazy { ServiceLocator(this) }

    override fun onCreate() {
        super.onCreate()
        MailResultNotifier.createChannel(this)
    }
}
