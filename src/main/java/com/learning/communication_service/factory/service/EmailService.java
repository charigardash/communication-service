package com.learning.communication_service.factory.service;

import com.learning.communication_service.factory.CommunicationChannelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("EMAIL")
public class EmailService implements CommunicationChannelService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOTP(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otp + "\nThis code will expire in 5 minutes.");
            mailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
