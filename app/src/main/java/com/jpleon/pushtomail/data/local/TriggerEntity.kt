package com.jpleon.pushtomail.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "triggers")
data class TriggerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val enabled: Boolean,
    val watchedPackages: List<String>,
    val smtpHost: String,
    val smtpPort: Int,
    val smtpUser: String,
    val useTls: Boolean,
    val fromEmail: String,
    val toEmail: String,
    val subjectTemplate: String,
    val bodyTemplate: String
)
