package com.fauzi.jobservice.util;

import com.fauzi.jobservice.configuration.exception.GeneralException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Log4j2
public class RSAUtil {

    private RSAUtil(){
    }
    public static PrivateKey getPrivateKey(String base64PrivateKey) {
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes()));

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
            return privateKey;
        } catch (InvalidKeySpecException var4) {
            log.error("InvalidKeySpecException : " + var4.getMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "5000", "InvalidKeySpecException : " + var4.getMessage());
        } catch (NoSuchAlgorithmException var5) {
            log.error("NoSuchAlgorithmException : " + var5.getMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR,"5000", "NoSuchAlgorithmException : " + var5.getMessage());
        }
    }

    public static PublicKey getPublicKey(String base64PublicKey) {
        PublicKey publicKey = null;
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (InvalidKeySpecException var4) {
            log.error("InvalidKeySpecException : " + var4.getMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR,"5000", "InvalidKeySpecException : " + var4.getMessage());
        } catch (NoSuchAlgorithmException var5) {
            log.error("NoSuchAlgorithmException : " + var5.getMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "5000", "NoSuchAlgorithmException : " + var5.getMessage());
        }
    }
}


