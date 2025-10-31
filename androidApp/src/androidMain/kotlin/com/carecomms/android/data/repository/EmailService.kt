package com.carecomms.android.data.repository

import com.carecomms.data.models.EmailInvitation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService(
    private val smtpHost: String = "smtp.gmail.com",
    private val smtpPort: String = "587",
    private val senderEmail: String,
    private val senderPassword: String
) {

    suspend fun sendInvitationEmail(emailInvitation: EmailInvitation): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", smtpHost)
                    put("mail.smtp.port", smtpPort)
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(senderEmail, senderPassword)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailInvitation.recipientEmail))
                    subject = "You're invited to CareComms by ${emailInvitation.carerName}"
                    
                    val htmlContent = createEmailTemplate(emailInvitation)
                    setContent(htmlContent, "text/html; charset=utf-8")
                }

                Transport.send(message)
                println("EmailService: Invitation email sent successfully to ${emailInvitation.recipientEmail}")
                Result.success(Unit)
                
            } catch (e: Exception) {
                println("EmailService: Error sending email: ${e.message}")
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private fun createEmailTemplate(emailInvitation: EmailInvitation): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>CareComms Invitation</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">CareComms</h1>
                    <p style="color: white; margin: 10px 0 0 0; font-size: 16px;">Care Communication Made Simple</p>
                </div>
                
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; border: 1px solid #ddd;">
                    <h2 style="color: #333; margin-top: 0;">You're Invited!</h2>
                    
                    <p style="font-size: 16px; margin-bottom: 20px;">
                        <strong>${emailInvitation.carerName}</strong> has invited you to join CareComms - a secure platform for care communication.
                    </p>
                    
                    <div style="background: white; padding: 20px; border-radius: 8px; border-left: 4px solid #667eea; margin: 20px 0;">
                        <p style="margin: 0; font-size: 14px; color: #666;">Your invitation code:</p>
                        <p style="font-size: 24px; font-weight: bold; color: #667eea; margin: 5px 0; letter-spacing: 2px;">${emailInvitation.invitationCode}</p>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="${emailInvitation.deepLink}" 
                           style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                                  color: white; 
                                  padding: 15px 30px; 
                                  text-decoration: none; 
                                  border-radius: 25px; 
                                  font-weight: bold; 
                                  font-size: 16px;
                                  display: inline-block;
                                  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
                                  margin: 5px;">
                            Open CareComms App
                        </a>
                        <br>
                        <a href="https://carecomms.app/invite/${emailInvitation.invitationCode}" 
                           style="background: #f0f0f0; 
                                  color: #333; 
                                  padding: 10px 20px; 
                                  text-decoration: none; 
                                  border-radius: 20px; 
                                  font-size: 14px;
                                  display: inline-block;
                                  margin: 5px;
                                  border: 1px solid #ddd;">
                            Alternative Link
                        </a>
                    </div>
                    
                    <div style="background: #e8f4f8; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="color: #2c5aa0; margin-top: 0; font-size: 16px;">How to get started:</h3>
                        <ol style="margin: 0; padding-left: 20px; color: #555;">
                            <li>Click the button above to open the CareComms app</li>
                            <li>If you don't have the app, you'll be directed to download it</li>
                            <li>Create your account or sign in</li>
                            <li>You'll automatically be connected with ${emailInvitation.carerName}</li>
                        </ol>
                    </div>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 30px;">
                        <strong>If the buttons don't work:</strong><br>
                        1. Download the CareComms app from your app store<br>
                        2. Create an account or sign in<br>
                        3. Manually enter the invitation code: <strong>${emailInvitation.invitationCode}</strong>
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #999; text-align: center; margin: 0;">
                        This invitation was sent by ${emailInvitation.carerName} through CareComms.<br>
                        If you didn't expect this invitation, you can safely ignore this email.
                    </p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}