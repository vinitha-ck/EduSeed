package com.vpk.eduseed
import java.util.Properties
import javax.mail.*
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GMailSender(private val username: String, private val password: String) {
    @Throws(MessagingException::class)
    fun sendMail(toEmail: String, subject: String, messageBody: String) {
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(username))
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
        message.subject = subject
        message.setText(messageBody)
        Transport.send(message)
    }
}
