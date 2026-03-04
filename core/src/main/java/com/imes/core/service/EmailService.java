package com.imes.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email Service for sending emails
 * Currently supports password reset emails
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@imes.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send password reset email
     * @param toEmail Recipient email address
     * @param token Password reset token
     * @param userName User's full name
     */
    public void sendPasswordResetEmail(String toEmail, String token, String userName) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            
            String subject = "IMES - Password Reset Request";
            String body = String.format("""
                    Hello %s,
                    
                    We received a request to reset your password for your IMES account.
                    
                    Click the link below to reset your password:
                    %s
                    
                    This link will expire in 1 hour.
                    
                    If you didn't request a password reset, please ignore this email or contact support if you have concerns.
                    
                    Best regards,
                    IMES Team
                    """, userName, resetUrl);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("[EMAIL-SERVICE] Password reset email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("[EMAIL-SERVICE] Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send welcome email after registration
     */
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            String subject = "Welcome to IMES!";
            String body = String.format("""
                    Hello %s,
                    
                    Welcome to IMES (Intern Management System)!
                    
                    Your account has been successfully created. You can now log in at:
                    %s/login
                    
                    If you have any questions, please contact your administrator.
                    
                    Best regards,
                    IMES Team
                    """, userName, frontendUrl);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("[EMAIL-SERVICE] Welcome email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("[EMAIL-SERVICE] Failed to send welcome email to {}: {}", toEmail, e.getMessage());
            // Don't throw exception for welcome email failure
        }
    }
}
