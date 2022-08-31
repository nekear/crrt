package com.github.DiachenkoMD.web.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.github.DiachenkoMD.entities.dto.users.LimitedUser;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.enums.ValidationParameters;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;


import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    private static final Logger logger = LogManager.getLogger(Utils.class);
    public static final  Random random = new Random();
    public static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Utils(){}


    /**
     * Method for capitalizing letters.
     * @param incoming - string to be capitalized
     * @return
     */
    public static String capitalize(String incoming){
        if(incoming != null){
            return incoming.substring(0,1).toUpperCase() + incoming.substring(1);
        }else{
            throw new IllegalArgumentException("Unable to capitalize *null*!");
        }
    }

    /**
     * Method for validating data depending on specified ValidationParameters parameter.
     * @param str - string to be validated
     * @param parameter - defines the validation pattern
     * @return true/false depending on whether validation was successful or not respectively
     */
    public static boolean validate(String str, ValidationParameters parameter){
        return Validatable.of(str, parameter, false).validate();
    }

    public static boolean validate(Validatable... args){
        if(args == null || args.length == 0)
            return false;

        return validate(Arrays.asList(args));
    }

    public static boolean validate(List<Validatable> args){
        if(args == null || args.size() == 0)
            return false;

        for(Validatable val : args){
            if(!val.validate()){
                System.out.println("VT FAIL: " + val.getValidationParameter() + " with data: " + val.getData());
                return false;
            }
        }
        return true;
    }

    /**
     * Method for one-way encrypting data. Uses salt from app.properties.
     * @param value - value to encrypt
     * @return encrypted string
     */
    public static String encryptPassword(String value){
        String salt = ResourceBundle.getBundle("app").getString("salt");

        return new String(BCrypt.withDefaults().hash(6, salt.getBytes(), value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * Uses BCrypt library (check maven dependencies) to encrypt passwords. Salt is taken from app.properties.
     * @param value
     * @param encrypted
     * @return
     */
    public static boolean encryptedPasswordsCompare(String value, String encrypted){
        return BCrypt.verifyer().verify(value.getBytes(StandardCharsets.UTF_8), encrypted.getBytes(StandardCharsets.UTF_8)).verified;
    }


    /**
     * Simple method for obtaining string (or null) from some source (I use it when getting parameters from query).
     * @param value
     * @return
     */
    public static String cleanGetString(String value){
        if(value == null)
            return null;
        if(value.isBlank())
            return null;

        return value.trim();
    }

    public static String generateRandomString(int bound){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = bound;
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int) (random.nextDouble() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    /**
     * Email notification util. Simplifies the thing it was created for. For now uses mailtrap as SMTP. Configure at app.properties.
     * @param user user mail will be sent to
     * @param subject subject of the mail
     * @param data content of the mail (might be some html, for example)
     */
    public static void emailNotify(LimitedUser user, String subject, String data){
        emailNotify(user.getEmail(), subject, data);
    }


    public static void emailNotify(String email, String subject, String data){
        ResourceBundle mailHostDataBundle = ResourceBundle.getBundle("app");

        emailNotify(email, subject, data,
                mailHostDataBundle.getString("mail.active").equalsIgnoreCase("true"));
    }

    /**
     * Email notification util. Simplifies the thing it was created for. For now uses mailtrap as SMTP. Configure at app.properties.
     * @param email
     * @param subject subject of the mail
     * @param data сontent of the mail (might be some html, for example)
     * @param isEnabled specifies whether to enable actual sending or to output messages to the console
     */
    public static void emailNotify(String email, String subject, String data, boolean isEnabled){
        ResourceBundle mailHostDataBundle = ResourceBundle.getBundle("app");

        if(isEnabled){
            String to = email;
            String from = mailHostDataBundle.getString("mail.from");
            String host = mailHostDataBundle.getString("mail.host");

            Properties props = System.getProperties();

            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.host", host);
            props.setProperty("mail.smtp.port", mailHostDataBundle.getString("mail.smtp.port"));


            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailHostDataBundle.getString("mail.smtp.user"), mailHostDataBundle.getString("mail.smtp.password"));
                }
            });

            try{
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject(subject.replaceAll("<.+?>", ""));

                Multipart multipart = new MimeMultipart("alternative");
                MimeBodyPart htmlBody = new MimeBodyPart();
                htmlBody.setContent(EmailFormatter.format(subject, data), "text/html; charset=utf-8");
                multipart.addBodyPart(htmlBody);

                message.setContent(multipart);
                message.saveChanges();

                Transport.send(message);

                logger.debug("Message to {} sent successfully.", to);
            }catch (MessagingException mex){
                logger.error(mex.getMessage());
            }
        }else{
            logger.info("Request for sending mail was blocked. Mail system is inactive!");
            logger.info("MAIL SUBJECT: {}", subject.replaceAll("<.+?>", ""));
            logger.info("MAIL BODY: {}", data);
        }
    }

    public static Optional<Cookie> getCookieFromArray(String name, Cookie[] cookies){
        Optional<Cookie[]> cookiesArray = Optional.ofNullable(cookies).filter(item -> item.length > 0);

        return cookiesArray.flatMap(value -> Arrays.stream(value).filter(cookie -> cookie.getName().equalsIgnoreCase(name)).findFirst());
    }

    public static Cookie createCookie(String name, String value, String path){
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        return cookie;
    }

    public static String getRoleTranslation(Roles role){
        return "roles."+role.keyword();
    }

    public static String getLang(HttpServletRequest req){
        return (String) req.getSession().getAttribute("lang");
    }

    public static boolean containsColumn(ResultSet rs, String column){
        try{
            rs.findColumn(column);
            return true;
        } catch (SQLException e){
            return false;
        }
    }

    public static void sendSuccess(String data, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(data);
        resp.getWriter().flush();
    }

    public static void sendException(String data, HttpServletResponse resp){
        try{
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(data);
            resp.getWriter().flush();
        }catch (IOException e){
            logger.error(e);
        }
    }

    public static String clean(String str){
        return str.trim().replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");
    }

    public static boolean multieq(String val, String... els){
        return Arrays.stream(els).parallel().filter(x -> x.equalsIgnoreCase(val)).toList().size() > 0;
    }
    public static boolean multieq(Object val, Object... els){
        return Arrays.stream(els).parallel().filter(x -> x.equals(val)).toList().size() > 0;
    }

    public static String getFileExtension(String name) {
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    public static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme() + "://";
        String serverName = request.getServerName();
        String serverPort = (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
        String contextPath = request.getContextPath();
        return scheme + serverName + serverPort + contextPath;
    }
}
