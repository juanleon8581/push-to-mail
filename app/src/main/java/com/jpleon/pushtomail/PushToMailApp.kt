package com.jpleon.pushtomail

import android.app.Application
import com.jpleon.pushtomail.di.ServiceLocator

class PushToMailApp : Application() {
    val serviceLocator by lazy { ServiceLocator(this) }
}
