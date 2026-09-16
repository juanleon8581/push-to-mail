package com.jpleon.pushtomail.mail

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmailTemplateEngine {
    fun render(template: String, appName: String, title: String, text: String): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        return template
            .replace("{{appName}}", appName)
            .replace("{{title}}", title)
            .replace("{{text}}", text)
            .replace("{{timestamp}}", timestamp)
    }
}
