package com.github.DiachenkoMD.web.utils;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.hmac.HMACVerifier;

import java.time.*;
import java.util.*;

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

    public static String encode(Map<String, ?> data, LocalDateTime expirationDT){
        ZoneId zoneId = ZoneId.of("Europe/Kiev");
        ZonedDateTime zdt = expirationDT.atZone(zoneId);

        JWT jwt = new JWT()
                .setIssuer(issuer)
                .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .setExpiration(zdt)
                .setUniqueId(ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() + Utils.generateRandomString(4));

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

    public static JWT decode(String jwtToken){
        return JWT.getDecoder().decode(jwtToken, verifier);
    }

    public static boolean disableToken(JWT token){
        return disabledTokens.add(token.uniqueId);
    }


    public static boolean isTokenAlive(JWT token){
        return !disabledTokens.contains(token.uniqueId);
    }
}
