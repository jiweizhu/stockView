package com.example.notification.util;

import com.example.notification.vo.StockNameVO;
import com.sun.mail.util.MailSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class EmailUtil {
    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    final static String from = "kiwi66@qq.com";
    final static String username = "kiwi66@qq.com";
    final static String password = "sczomovkggdlcbcd";
    final static String host = "smtp.qq.com";
    final static String port = "465";
    final static String to = "370610613@qq.com";
    final static String title = "NumAlarm";

    static Properties prop = new Properties();

    static {
        prop.setProperty("mail.smtp.host", host);
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.transport.port", port);
        prop.setProperty("mail.smtp.starttls.enable", "true");
        prop.setProperty("mail.smtp.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.socketFactory.port", port);
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.put("mail.smtp.socketFactory.fallback", "false");
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
        } catch (GeneralSecurityException e) {
            logger.error("===========MailSSLSocketFactory failed, error is ", e);
        }
        prop.put("mail.smtp.ssl.socketFactory", sf);
        sf.setTrustAllHosts(true);
    }

    public static void main(String[] args) throws Exception {
        StockNameVO stockNameVO = new StockNameVO();
        sendMailSingle(stockNameVO);
    }

    public static void sendMailSingle(StockNameVO stockNameVO) throws Exception {
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


        String nowTime = setMessage(stockNameVO, message);

        // send mail
        ts.sendMessage(message, message.getAllRecipients());
        logger.info("Mail sent successfully=====" + nowTime + "====" + stockNameVO);
        // release resource
        ts.close();
    }

    private static String setMessage(StockNameVO stockNameVO, MimeMessage message) throws MessagingException {
        String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        message.setContent("Now the time is " + nowTime + " , this is a important message!" + " Exceed 10 day avg price is " + stockNameVO.getStockId() + ", " + stockNameVO.getStockName(), "text/html;charset=utf-8");
        return nowTime;
    }

    public static void sendMail(Map<String, StockNameVO> upTenDayMap, Map<String, StockNameVO> downTenDayMap) throws Exception {
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


        String nowTime = setMapMessage(upTenDayMap, downTenDayMap, message);

        // send mail
        ts.sendMessage(message, message.getAllRecipients());
        logger.info("Mail sent successfully=====" + nowTime + "=upTenDayMap==={},========downTenDayMap==={}", upTenDayMap, downTenDayMap);
        // release resource
        ts.close();
    }

    private static String setMapMessage(Map<String, StockNameVO> upTenDayMap, Map<String, StockNameVO> downTenDayMap, MimeMessage message) throws MessagingException {
        String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        AtomicInteger upCount = new AtomicInteger(0);
        final StringBuffer upTenDayStock = new StringBuffer();
        upTenDayMap.keySet().forEach(k -> {
            upTenDayStock.append(" " + upCount.incrementAndGet() + "." + upTenDayMap.get(k)).append("<br>");
        });

        AtomicInteger downCount = new AtomicInteger(0);
        final StringBuffer downTenDayStock = new StringBuffer();
        downTenDayMap.keySet().forEach(k -> {
            downTenDayStock.append(" " + downCount.incrementAndGet() + "." + downTenDayMap.get(k)).append("<br>");
        });
        message.setContent("Now the time is <b>" + nowTime + "</b>, this is a important message!<br>" +
                        "=================================<br>" +
                        "<b>Down 10 day avg price: </b><br>" + downTenDayStock +
                        "=================================<br>" +
                        "<b>Exceed 10 day avg price: </b><br>" + upTenDayStock,
                "text/html;charset=utf-8");
        return nowTime;
    }

}
