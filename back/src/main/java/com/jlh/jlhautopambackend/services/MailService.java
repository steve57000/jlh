package com.jlh.jlhautopambackend.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service @RequiredArgsConstructor
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSender mailSender;

    @Value("${app.email.from}") String from;
    @Value("${app.email.enabled:true}") boolean emailEnabled;

    public void sendHtml(String to, String subject, String html) {
        if (!emailEnabled) {
            log.info("Envoi d'e-mail désactivé (destinataire={}, sujet={})", to, subject);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new IllegalStateException("Envoi mail échoué", e);
        }
    }
}
