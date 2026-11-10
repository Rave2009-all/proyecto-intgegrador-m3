package com.example.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Properties;

@Service
public class CorreoService {

    @Value("${BOOKLY_EMAIL}")
    private String correo;

    @Value("${BOOKLY_EMAIL_PASSWORD}")
    private String clave;

    public void enviarCorreo(
            String destinatario,
            String asunto,
            String mensaje
    ) throws Exception {

        if (correo == null || clave == null || correo.isEmpty() || clave.isEmpty()) {
            throw new Exception("Faltan las credenciales del correo");
        }

        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.port", "587");

        Session session = Session.getInstance(propiedades, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(correo, clave);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(correo));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject(asunto);
        message.setText(mensaje);

        Transport.send(message);
    }
}