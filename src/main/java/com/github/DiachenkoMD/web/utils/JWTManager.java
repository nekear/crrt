package com.github.DiachenkoMD.web.utils;

import io.fusionauth.jwt.InvalidJWTException;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.hmac.HMACVerifier;

import java.time.*;
import java.util.*;

/**
 * Utility for managing JWT tokens. Uses FusionAuth-JWT under the hood. <br/>
 * @implNote <strong>JWT secret</strong> and <strong>JWT issuer</strong> name are obtained from app.properties. So, be sure, to mention them.
 */
public class JWTManager {
    private static final Set<String> disabledTokens = new HashSet<>();
    private static final String secret, issuer;
    private static final Signer signer;
    private static final Verifier verifier;

    static {
        ResourceBundle appProps = ResourceBundle.getBundle("app");
        secret = appProps.getString("jwt.secret");
        issuer = appProps.getString("jwt.issuer");
        signer = HMACSigner.newSHA256Signer(secret);
        verifier = HMACVerifier.newVerifier(secret);
    }

    /**
     * Method for encoding specified data into JWT token.
     * @param data
     * @param expirationDT expiration date of token. Might be null if expiration date should not be specified.
     * @return
     */
    public static String encode(Map<String, ?> data, LocalDateTime expirationDT){
        JWT jwt = new JWT()
                .setIssuer(issuer)
                .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .setUniqueId(ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() + Utils.generateRandomString(4));

        if(expirationDT != null){
            ZoneId zoneId = ZoneId.of("Europe/Kiev");
            ZonedDateTime zdt = expirationDT.atZone(zoneId);

            jwt.setExpiration(zdt);
        }

        data.forEach(jwt::addClaim);

        return JWT.getEncoder().encode(jwt, signer);
    }

    public static String encode(String key, Object value, LocalDateTime expirationDT){
        return encode(Map.of(key, value), expirationDT);
    }

    public static String encode(String key, Object value){
        return encode(Map.of(key, value), LocalDateTime.now().plusDays(1));
    }

    public static String encode(Map<String, ?> dataMap){
        return encode(dataMap, LocalDateTime.now().plusDays(1));
    }

    /**
     * Method for decoding JWT tokens. Because of FusionAuth realization of decoding, developer should handle mentioned exceptions, which are inherited from RuntimeException.
     * @param jwtToken
     * @return decoded JWT token.
     * @throws JWTExpiredException
     * @throws InvalidJWTException
     */
    public static JWT decode(String jwtToken) throws JWTExpiredException, InvalidJWTException {
        return JWT.getDecoder().decode(jwtToken, verifier);
    }

    /**
     * Method for disabling tokens. Disabling is being done by storing unique token id`s inside Set.
     * @param token
     * @return true if token id was added to set and false otherwise.
     */
    public static boolean disableToken(JWT token){
        return disabledTokens.add(token.uniqueId);
    }


    /**
     * Method for checking whether token has been disabled or not.
     * @param token
     * @return
     */
    public static boolean isTokenAlive(JWT token){
        return !disabledTokens.contains(token.uniqueId);
    }

    /**
     * Method for clearing disabled tokens storage.
     */
    public static void emptyDisabledTokensList(){
        disabledTokens.clear();
    }
}
