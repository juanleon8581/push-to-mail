package com.jpleon.pushtomail.mail

import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Blocking SMTP send via JavaMail (com.sun.mail:android-mail). Must be called
 * off the main thread -- MailWorker (a CoroutineWorker) is the only caller.
 */
class SmtpMailSender {

    fun send(
        host: String,
        port: Int,
        user: String,
        password: String,
        useTls: Boolean,
        from: String,
        to: String,
        subject: String,
        body: String
    ) {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", useTls.toString())
        }

        val session = Session.getInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(user, password)
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject)
            setText(body)
        }

        Transport.send(message)
    }
}
