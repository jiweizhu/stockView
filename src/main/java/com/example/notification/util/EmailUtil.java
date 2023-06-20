package com.example.notification.util;

import com.example.notification.vo.StockNameVO;
import com.sun.mail.util.MailSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailUtil {
    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    final static String from = "kiwi66@qq.com";
    final static String username = "kiwi66@qq.com";
    final static String password = "sczomovkggdlcbcd";
    final static String host = "smtp.qq.com";
    final static String to = "370610613@qq.com";
    final static String title = "NumAlarm";


    public static void main(String[] args) throws Exception {
        StockNameVO stockNameVO = new StockNameVO();
        sendMail(stockNameVO);
    }

    public static void sendMail(StockNameVO stockNameVO) throws Exception {

        Properties prop = new Properties();
        prop.setProperty("mail.smtp.host", "smtp.qq.com");
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);

        Session session = Session.getDefaultInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(username, password);
            }
        });
//        session.setDebug(true);
        Transport ts = session.getTransport();
        ts.connect(host, username, password);

        //create mail content
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(title);
        String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        message.setContent("Now the time is "+ nowTime +" , this is a important message!" +
                "Exceed 10 day avg price is "+ stockNameVO.getIdentifier() +", "+ stockNameVO.getChineseName(), "text/html;charset=utf-8");

        // send mail
        ts.sendMessage(message, message.getAllRecipients());
        logger.info("Mail sent successfully====="+ nowTime+"===="+ stockNameVO);
        // release resource
        ts.close();


    }

}
