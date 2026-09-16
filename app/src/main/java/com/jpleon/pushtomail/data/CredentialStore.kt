package com.jpleon.pushtomail.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * SMTP passwords live here (encrypted), never in the Room database, keyed by trigger id.
 */
class CredentialStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "smtp_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePassword(triggerId: Long, password: String) {
        prefs.edit().putString(keyFor(triggerId), password).apply()
    }

    fun getPassword(triggerId: Long): String? = prefs.getString(keyFor(triggerId), null)

    fun deletePassword(triggerId: Long) {
        prefs.edit().remove(keyFor(triggerId)).apply()
    }

    private fun keyFor(triggerId: Long) = "smtp_password_$triggerId"
}
