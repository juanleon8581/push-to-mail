package com.jpleon.pushtomail.data

import com.jpleon.pushtomail.data.local.TriggerDao
import com.jpleon.pushtomail.data.local.TriggerEntity
import kotlinx.coroutines.flow.Flow

data class Trigger(
    val id: Long,
    val name: String,
    val enabled: Boolean,
    val watchedPackages: List<String>,
    val smtpHost: String,
    val smtpPort: Int,
    val smtpUser: String,
    val smtpPassword: String,
    val useTls: Boolean,
    val fromEmail: String,
    val toEmail: String,
    val subjectTemplate: String,
    val bodyTemplate: String
)

/**
 * Joins the Room-backed trigger config with its SMTP password from CredentialStore
 * so callers work with one complete domain model.
 */
class TriggerRepository(
    private val dao: TriggerDao,
    private val credentialStore: CredentialStore
) {
    fun observeAll(): Flow<List<TriggerEntity>> = dao.observeAll()

    suspend fun getEnabledForPackage(packageName: String): List<Trigger> =
        dao.getEnabled()
            .filter { packageName in it.watchedPackages }
            .map { it.toDomain() }

    suspend fun getById(id: Long): Trigger? = dao.getById(id)?.toDomain()

    suspend fun save(trigger: Trigger): Long {
        val id = dao.upsert(trigger.toEntity())
        credentialStore.savePassword(if (trigger.id == 0L) id else trigger.id, trigger.smtpPassword)
        return id
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) = dao.setEnabled(id, enabled)

    suspend fun deleteById(id: Long) {
        dao.getById(id)?.let { entity ->
            dao.delete(entity)
            credentialStore.deletePassword(id)
        }
    }

    private fun TriggerEntity.toDomain() = Trigger(
        id = id, name = name, enabled = enabled, watchedPackages = watchedPackages,
        smtpHost = smtpHost, smtpPort = smtpPort, smtpUser = smtpUser,
        smtpPassword = credentialStore.getPassword(id).orEmpty(),
        useTls = useTls, fromEmail = fromEmail, toEmail = toEmail,
        subjectTemplate = subjectTemplate, bodyTemplate = bodyTemplate
    )

    private fun Trigger.toEntity() = TriggerEntity(
        id = id, name = name, enabled = enabled, watchedPackages = watchedPackages,
        smtpHost = smtpHost, smtpPort = smtpPort, smtpUser = smtpUser,
        useTls = useTls, fromEmail = fromEmail, toEmail = toEmail,
        subjectTemplate = subjectTemplate, bodyTemplate = bodyTemplate
    )
}
