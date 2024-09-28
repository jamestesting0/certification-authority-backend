package com.chuk_ca.sprint_initializer.service.util;

import jakarta.mail.MessagingException;
import org.springframework.core.io.InputStreamSource;

import java.io.IOException;

public interface EmailSender {
    void sendEmail(String address, String subject, String text, InputStreamSource file) throws MessagingException, IOException;
}
