package com.bittokazi.watch.navigator.mail.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MailGeneratorService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        log.info("[MailGeneratorService] -> init")
    }

    fun generateResetPasswordMail(data: Map<ResetPasswordMailLabel, String>): String {
        val username = data[ResetPasswordMailLabel.USERNAME] ?: "User"
        val resetLink = data[ResetPasswordMailLabel.RESET_LINK] ?: "#"

        return """
            <html>
                <body>
                    <p>Hi $username,</p>
                    <p>You requested a password reset. Click the link below to reset your password:</p>
                    <a href="$resetLink">Reset Password</a>
                    <p>If you did not request this, please ignore this email.</p>
                    <p>Best regards,<br/>Zepp Transport Timetable App Team</p>
                </body>
            </html>
            """.trimIndent()
    }

    fun generateWelcomeEmail(data: Map<WelcomeEmailLabel, String>): String {
        val username = data[WelcomeEmailLabel.USERNAME] ?: "User"
        val limit = data[WelcomeEmailLabel.LIMIT] ?: "10"

        return """
            <html>
                <body>
                    <p>Hi $username,</p>
                    <p>Welcome to Zepp Transport Timetable App</p>
                    <p>Now you can connect your watch with this account through the app settings in zepp app</p>
                    <p>You can make $limit monthly calls for trip plan</p>
                    <p>Best regards,<br/>Zepp Transport Timetable App Team</p>
                </body>
            </html>
            """.trimIndent()
    }
}

enum class ResetPasswordMailLabel {
    USERNAME,
    RESET_LINK,
}

enum class WelcomeEmailLabel {
    USERNAME,
    LIMIT
}
