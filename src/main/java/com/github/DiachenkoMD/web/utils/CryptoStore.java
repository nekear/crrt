package com.github.DiachenkoMD.web.utils;

import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.entities.exceptions.ExceptionReason;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility to easily encrypt and decrypt sensitive data. <br/>
 * At this project, primarily used at encrypting entities`s ids while converting them to JSON. </br>
 * @implSpec Salt is generated randomly each time the server boots.
 * @apiNote You can freely use {@link #encrypt(String)} method for encrypting data and {@link #decrypt(String)} for decrypting.
 */
public class CryptoStore {
    private static SecretKey key;
    private static IvParameterSpec iv;


    public static String encrypt(String value) throws DescriptiveException{
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), getIv());
            byte[] cipherText = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        }catch (Exception e){
            throw new DescriptiveException(e.getMessage(), ExceptionReason.CRYPTO_OPERATION_ERROR);
        }
    }

    public static String decrypt(String value) throws DescriptiveException{
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(), getIv());
            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(value));
            return new String(plainText);
        }catch (Exception e){
            throw new DescriptiveException(e.getMessage(), ExceptionReason.CRYPTO_OPERATION_ERROR);
        }
    }

    private static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(128);
        return keygen.generateKey();
    }

    private static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static synchronized SecretKey getKey() throws NoSuchAlgorithmException{
        if(key == null)
            key = generateKey();

        return key;
    }

    public static synchronized IvParameterSpec getIv(){
        if(iv == null)
            iv = generateIv();

        return iv;
    }
}
