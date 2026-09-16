package com.jpleon.pushtomail.di

import android.content.Context
import com.jpleon.pushtomail.data.CredentialStore
import com.jpleon.pushtomail.data.TriggerRepository
import com.jpleon.pushtomail.data.local.AppDatabase

/**
 * Manual DI container hung off the Application subclass. No Hilt/Koin on purpose
 * (keeps the build free of annotation-processing surprises for a first scaffold).
 */
class ServiceLocator(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy { AppDatabase.build(appContext) }
    val credentialStore: CredentialStore by lazy { CredentialStore(appContext) }
    val triggerRepository: TriggerRepository by lazy {
        TriggerRepository(database.triggerDao(), credentialStore)
    }
}
