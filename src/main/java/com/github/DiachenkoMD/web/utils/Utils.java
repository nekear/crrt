package com.github.DiachenkoMD.web.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.github.DiachenkoMD.entities.dto.User;
import com.github.DiachenkoMD.entities.enums.ValidationParameters;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.*;
import java.util.regex.Pattern;

public class Utils {
    private static final Logger logger = LogManager.getLogger(Utils.class);
    public static final  Random random = new Random();

    private Utils(){}
    /**
     * Method for parsing json object to specified parsingDestination. The parsingDestination should have methods like set[key from json object] to let this function fill the necessary fields.<br>
     * Spoiler: at the end, I decided to switch to GSON library, so that method is useless, but I want to leave in here for future use in different projects.
     * @param parsedJsonObject - parsed to json object
     * @param parsingDestination - Class reference to which method should parse data
     * @return new class instance with data from json object
     * @param <T>
     */
    public static <T> T flatJsonParser(JSONObject parsedJsonObject, Class<T> parsingDestination) {
        try {
            Iterator<String> it = parsedJsonObject.keys();

            T parsingDestInstance = parsingDestination.getConstructor().newInstance();

            int failedMethodsToInvoke = 0;

            while (it.hasNext()) {
                String key = it.next();
                Object value = parsedJsonObject.get(key);

                Class<?> classType;

                if (value instanceof Boolean) {
                    classType = Boolean.class;
                } else if (value instanceof Number) {
                    classType = Number.class;
                } else {
                    classType = String.class;
                }

                try{
                    Method settingMethod = parsingDestination.getDeclaredMethod("set" + (capitalize(key)), classType);

                    settingMethod.invoke(parsingDestInstance, value);
                }catch (NoSuchMethodException | SecurityException ignored){
                    ++failedMethodsToInvoke;
                }

            }

            return failedMethodsToInvoke == parsedJsonObject.length() ? null : parsingDestInstance;
        }catch (NoSuchMethodException| InvocationTargetException | IllegalAccessException | InstantiationException e){
            throw new IllegalStateException(String.format("[FJP] -> [Dest: %s] Exception occured while parsing %s", parsingDestination.getName(), parsedJsonObject), e);
        }
    }
    public static <T> T flatJsonParser(String json, Class<T> parsingDestination) {
        return flatJsonParser(new JSONObject(json), parsingDestination);
    }

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
     * Method from checking for equality of two objects using reflection. Main purpose was to use in unit testing, but it can be used anywhere else.
     * @param obj1
     * @param obj2
     * @return
     * @param <T>
     */
    public static <T> boolean reflectiveEquals(T obj1, T obj2){
        Field[] obj1Fields = obj1.getClass().getDeclaredFields();
        Field[] obj2Fields = obj2.getClass().getDeclaredFields();

        try{
            for(int i = 0; i < obj1Fields.length; i++){
                Class<?> currentType = obj1Fields[i].getType();

                Field f1 = obj1Fields[i];
                Field f2 = obj2Fields[i];

                f1.setAccessible(true);
                f2.setAccessible(true);

                if(f1.get(obj1) != null && f2.get(obj2) != null) {
                    if (currentType.equals(String.class) && !((String) f1.get(obj1)).equalsIgnoreCase((String) f2.get(obj2)))
                        return false;
                    else if (currentType.equals(Number.class) && f1.getDouble(obj1) != f2.getDouble(obj2))
                        return false;
                    else if (currentType.equals(Boolean.class) && f1.getBoolean(obj1) != f2.getBoolean(obj2))
                        return false;
                    else if (!f1.get(obj1).equals(f2.get(obj2)))
                        return false;
                }else if(f1.get(obj1) == null && f2.get(obj2) != null || f1.get(obj2) == null && f2.get(obj1) != null) {
                    return false;
                }
            }

            return true;
        }catch (IllegalAccessException e){
            throw new IllegalStateException(String.format("[RE] Exception caught while comparing %s and %s", obj1, obj2), e);
        }
    }

    /**
     * Method for validating data depending on specified ValidationParameters parameter.
     * @param str - string to be validated
     * @param parameter - defines the validation pattern
     * @return true/false depending on whether validation was successful or not respectively
     */
    public static boolean validate(String str, ValidationParameters parameter){
        if(str == null)
            return false;

        Pattern pattern = null;

        switch (parameter){
            case NAME -> pattern = Pattern.compile("[a-zA-ZА-ЩЬЮЯҐЄІЇа-щьюяґєії'`]+");
            case EMAIL -> pattern = Pattern.compile("\\w+@[a-zA-Z0-9]+\\.[a-z]+");
            case PASSWORD -> pattern = Pattern.compile("(?=.*\\d)[a-zA-Z\\d]{4,}$");
        }

        return pattern != null && pattern.matcher(str).matches();
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
     * @param user - user mail will be sent to
     * @param subject - subject of the mail
     * @param data - content of the mail (might be some html, for example)
     */
    public static void emailNotify(User user, String subject, String data){

        ResourceBundle rb = ResourceBundle.getBundle("app");

        if(rb.getString("mail.active").equalsIgnoreCase("true")){
            String to = user.getEmail();
            String from = rb.getString("mail.from");
            String host = rb.getString("mail.host");

            Properties props = System.getProperties();

            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.host", host);
            props.setProperty("mail.smtp.port", rb.getString("mail.smtp.port"));


            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(rb.getString("mail.smtp.user"), rb.getString("mail.smtp.password"));
                }
            });

            try{
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject(subject);
                message.setText(data);

                Transport.send(message);

                logger.debug("Message to {} sent successfully.", to);
            }catch (MessagingException mex){
                logger.error(mex.getMessage());
            }
        }else{
            logger.info("Request for sending mail was blocked. Mail system is inactive!");
            logger.info("MAIL SUBJECT: {}", subject);
            logger.info("MAIL BODY: {}", data);
        }
    }

    public static Optional<Cookie> getCookieFromArray(String name, Cookie[] cookies){
        Optional<Cookie[]> cookiesArray = Optional.ofNullable(cookies).filter(item -> item.length > 0);

        return cookiesArray.flatMap(value -> Arrays.stream(value).filter(cookie -> cookie.getName().equalsIgnoreCase(name)).findFirst());
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
}
