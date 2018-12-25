package com.karmazin.model;

import com.karmazin.controller.WorkScreen;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

/**
 * The class allows you to send a letter "letter.html" on email to the user
 * To work, you need "gmail" mail service itself, on behalf of which will go
 * letters. For testing, you need to include permission to use in the mail
 * mail from third-party software.
 */
public class SendHTMLEmail {

    private static Properties mailServerProperties;
    private static Session getMailSession;
    private static MimeMessage generateMailMessage;

    // Enter your correct gmail UserID and Password
    private String serverGmailID;
    private String serverPassword;

    public SendHTMLEmail(String serverGmailID, String serverPassword) {
        this.serverGmailID = serverGmailID;
        this.serverPassword = serverPassword;
    }

    /**
     * Method sends letter "letter.html" on specified email
     *
     * @param email recipient email
     */
    public void localOverloadMessage(String email, String theme) throws AddressException, MessagingException {
        if (email != null && email.length() > 6) {
            // Step1
            //System.out.println("Установка настроек почты...");
            mailServerProperties = System.getProperties();
            mailServerProperties.put("mail.smtp.port", "587");
            mailServerProperties.put("mail.smtp.auth", "true");
            mailServerProperties.put("mail.smtp.starttls.enable", "true");
            //System.out.println("Установка настроек почты прошла успешно");

            // Step2
            //System.out.println("Получаем почтовую сессию...");
            getMailSession = Session.getDefaultInstance(mailServerProperties, null);
            generateMailMessage = new MimeMessage(getMailSession);
            generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

            generateMailMessage.setSubject(theme);
            // --------------------------------------------------------------------------------------------------
            // Content collector
            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();

            // Adding picture with logo
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(new FileDataSource("src/main/resources/logs/sysload.cfg")));
            messageBodyPart.setHeader("Content-ID", "<doc>");
            multipart.addBodyPart(messageBodyPart);

            // Adding everything in public container
            generateMailMessage.setContent(multipart);
            // --------------------------------------------------------------------------------------------------

            // Step3
            Transport transport = getMailSession.getTransport("smtp");

            // if you have 2FA enabled then provide App Specific Password
            transport.connect("smtp.gmail.com", serverGmailID, serverPassword);
            transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
            transport.close();
        }
    }

    public void serverFailureMessage(String email, String theme, String IP) throws MessagingException {
        if (email != null && email.length() > 6) {
            // Step1
            //System.out.println("Установка настроек почты...");
            mailServerProperties = System.getProperties();
            mailServerProperties.put("mail.smtp.port", "587");
            mailServerProperties.put("mail.smtp.auth", "true");
            mailServerProperties.put("mail.smtp.starttls.enable", "true");
            //System.out.println("Установка настроек почты прошла успешно");

            // Step2
            //System.out.println("Получаем почтовую сессию...");
            getMailSession = Session.getDefaultInstance(mailServerProperties, null);
            generateMailMessage = new MimeMessage(getMailSession);
            generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

            generateMailMessage.setSubject(theme);
            // --------------------------------------------------------------------------------------------------
            // Content collector
            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();

            // Recieving server's info
            ServerDataContainer serverData = WorkScreen.getServerData(IP);

            // Setting http-text of email
            String content = emailContent();
            Pattern pattern = Pattern.compile("\\{coord\\}");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                content = matcher.replaceAll(serverData.getGeodata());
            }

            // Adding page-proofs
            messageBodyPart.setContent(content, "text/html;charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);

            // Adding picture with logo
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(new FileDataSource("src/main/resources/pngs/PapichLogo.png")));
            messageBodyPart.setHeader("Content-ID", "<logo>");
            multipart.addBodyPart(messageBodyPart);


            // Adding picture from vk
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(new FileDataSource("src/main/resources/pngs/vk.png")));
            messageBodyPart.setHeader("Content-ID", "<vk>");
            multipart.addBodyPart(messageBodyPart);

            // Adding picture with map
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(serverData.getMap(), "image/png")));
            messageBodyPart.setHeader("Content-ID", "<map>");
            multipart.addBodyPart(messageBodyPart);

            // Adding picture with plot
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(serverData.getChart(), "image/png")));
            messageBodyPart.setHeader("Content-ID", "<graph>");
            multipart.addBodyPart(messageBodyPart);

            // Adding everything in public container
            generateMailMessage.setContent(multipart);
            // --------------------------------------------------------------------------------------------------

            // Step3
            Transport transport = getMailSession.getTransport("smtp");

            // if you have 2FA enabled then provide App Specific Password
            transport.connect("smtp.gmail.com", serverGmailID, serverPassword);
            transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
            transport.close();
        }
    }

    /**
     * Method build string from html document
     *
     * @return return html string
     */
    private String emailContent() {
        String html = "";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get("src/main/resources/html/letter.html"));
            html = new String(encoded, Charset.defaultCharset());
        } catch (IOException e) { }

        return  html;
    }

}