package com.github.DiachenkoMD.web.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RecaptchaVerifier {
    private final static Logger logger = LogManager.getLogger(RecaptchaVerifier.class);
    private static final String url, realSiteKey, testSiteKey;
    private static boolean isInTestingMode;

    static {
        ResourceBundle appProps = ResourceBundle.getBundle("app");
        url = appProps.getString("recaptcha.verifier.url");
        realSiteKey = appProps.getString("recaptcha.secretKey");
        testSiteKey = appProps.getString("test.recaptcha.secretKey");
        isInTestingMode = false;
    }

    public static void setRealMode(){
        isInTestingMode = false;
    }

    public static void setTestingMode(){
        isInTestingMode = true;
    }

    public static boolean verify(String captchaCode) throws IOException{
        if(captchaCode == null || captchaCode.isBlank())
            return false;

        URL verificationServiceUrl = new URL(url);

        HttpsURLConnection serviceConnection = (HttpsURLConnection) verificationServiceUrl.openConnection();

        serviceConnection.setRequestMethod("POST");

        // ** SETTING UP SITE KEY DEPENDING ON MODE ** //
        String siteKey = isInTestingMode ? testSiteKey : realSiteKey;

        String params = String.format("secret=%s&response=%s", siteKey, captchaCode);

        // Sending data to Google captcha API
        serviceConnection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(serviceConnection.getOutputStream());
        outputStream.writeBytes(params);
        outputStream.flush();
        outputStream.close();

        logger.debug("Request URL: {}?{}", url, params);

        // Retrieving response from that API (should be boolean)
        BufferedReader inputStream = new BufferedReader(
                new InputStreamReader(
                        serviceConnection.getInputStream()
                )
        );

        String acquiredDataStr = inputStream.lines().collect(Collectors.joining("\n"));
        inputStream.close();

        logger.debug("Retrieved response: {}", acquiredDataStr);

        // Parsing obtained json and returning verification status
        JSONObject json = new JSONObject(acquiredDataStr);
        return json.getBoolean("success");
    }
}
