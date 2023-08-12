package com.fauzi.jobservice.util;

import com.fauzi.jobservice.configuration.exception.GeneralException;
import com.fauzi.jobservice.model.response.ExpiryDetails;
import com.fauzi.jobservice.model.response.TokenDetails;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpStatus;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class TokenUtils {
    private TokenUtils(){
    }

    public static TokenDetails generateToken(String username, String email,
                                             String kid, String privateKey, String publicKey) {
        try {

            RSAKey rsaJWK = new RSAKey.Builder((RSAPublicKey) keyPair(privateKey, publicKey).getPublic())
                    .privateKey(keyPair(privateKey, publicKey).getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(kid)
                    .build();

            JWSSigner signer = new RSASSASigner(rsaJWK);

            var expiryDetails = getExpiryTime("3600");
            JWSObject jwsObject = new JWSObject(constructJWSHeader(kid),
                    createPayload(username, email, expiryDetails.getExpiryDate()));

            jwsObject.sign(signer);

            String s = jwsObject.serialize();
            jwsObject = JWSObject.parse(s);
            return TokenDetails.builder()
                    .accessToken(jwsObject.serialize())
                    .expiryDetails(expiryDetails)
                    .build();
        } catch (ParseException | JOSEException e) {
            log.error(e.getMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR,"50000","Error when create token");
        }
    }

    private static ExpiryDetails getExpiryTime(String validityDb) {
        Integer validity = Integer.parseInt(validityDb);
        Date expiryTime = new Date(System.currentTimeMillis());
        expiryTime = DateUtils.addSeconds(expiryTime, validity);
        return ExpiryDetails.builder()
                .expiryDate(expiryTime)
                .expiry(expiryTime.getTime())
                .build();
    }

    private static KeyPair keyPair(String privateKeyValue, String publicKeyValue){
        PrivateKey privateKey = RSAUtil.getPrivateKey(privateKeyValue);
        PublicKey publicKey = RSAUtil.getPublicKey(publicKeyValue);
        return new KeyPair(publicKey, privateKey);
    }

    private static JWSHeader constructJWSHeader(String kid) {
        return new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(kid)
                .build();
    }

    private static Payload createPayload(String username, String email, Date expiry) {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("iss", "dans");
        claimMap.put("user_name", username);
        claimMap.put("email", email);
        claimMap.put("scope", "login");
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.expirationTime(expiry)
                .issueTime(new Date())
                .audience("dans");
        claimMap.forEach(builder::claim);
        JWTClaimsSet claims = builder.build();
        return new Payload(claims.toJSONObject());
    }

    public static Map<String, String> decodeToken(String jwtToken) {
        Map<String, String> body = new HashMap<>();

        try {
            if (jwtToken != null) {
                jwtToken = jwtToken.replace("Bearer", "");
                jwtToken = jwtToken.trim();
            }
            if (StringUtils.isBlank(jwtToken)) {
                return body;
            }
            JWSObject token = JWSObject.parse(jwtToken);

            Payload tokenPayload = token.getPayload();
            JSONObject tokenBody = (JSONObject) tokenPayload.toJSONObject();

            tokenBody.forEach((key, value) -> {
                if (Objects.isNull(value)) {
                    value = "";
                }

                body.put(key, value.toString());
            });
        } catch (Exception e) {
            log.error("Failed to parse JWT Token. Error: {}", e.getMessage());
        }

        return body;
    }
}
