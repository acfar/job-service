package com.fauzi.jobservice.apisecurity.processor;

import com.fauzi.jobservice.apisecurity.model.LoginScopes;
import com.fauzi.jobservice.apisecurity.model.ScopeType;
import com.fauzi.jobservice.configuration.exception.GeneralException;
import com.fauzi.jobservice.model.entity.User;
import com.fauzi.jobservice.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.fauzi.jobservice.util.RSAUtil.getPrivateKey;
import static com.fauzi.jobservice.util.RSAUtil.getPublicKey;
import static com.fauzi.jobservice.util.TokenUtils.decodeToken;

@Log4j2
public class TokenValidator {

    @Value("${api-security.global.kid}")
    private String kid;

    @Value("${api-security.global.private-key}")
    private String privateKeyValue;

    @Value("${api-security.global.public-key}")
    private String publicKeyValue;

    @Autowired
    HttpServletRequest request;

    @Autowired
    UserRepository userRepository;

    private JWKSource<SecurityContext> internalKey;
    ConfigurableJWTProcessor<SecurityContext> internalJwtProcessor;

    public TokenValidator(
    ) {
        initInternalTokenProcessor();
    }

    public void validateToken(Map<ScopeType, LoginScopes[]> apiScopes, HttpServletRequest request, String body) {
        if (Objects.nonNull(apiScopes.get(ScopeType.CUSTOMER))) {
            if (Stream.of(apiScopes.get(ScopeType.CUSTOMER))
                    .anyMatch(scope ->
                            StringUtils.equalsIgnoreCase(scope.getValue(), LoginScopes.PUBLIC.getValue()))) {
                return;
            }
        }
        checkInitProcessor();
        String authorizationData = request.getHeader("Authorization");
        if (authorizationData == null){
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "Unable to get the Token - Invalid Token");
        }
        String token = authorizationData.replace("Bearer", "");
        token = token.trim();
        if (token == null) {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "Unable to get the Token - Invalid Token");
        }

        Map<String, String> tokenBody = decodeToken(token);
        String issuedBy = tokenBody.get("iss");
        String userName = tokenBody.get("user_name");

        User user = userRepository.findByUsername(userName).orElseThrow(()->{
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "Token is not valid (username not found)");
        });

        if (issuedBy == null) throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "Unable to get the Token Issuer - Invalid Token");

        //check signature
        this.checkSignature(token);

        //check token expiration
        this.checkTokenExpiration(token);

        //check token scope
        LoginScopes[] loginScopes = getScopesBasedOnType(apiScopes, issuedBy);

        this.checkScopes(token, loginScopes);
    }

    private LoginScopes[] getScopesBasedOnType(Map<ScopeType, LoginScopes[]> apiScopes, String issuedBy) {
        LoginScopes[] loginScopes = null;

        if (StringUtils.equalsIgnoreCase("dans", issuedBy)){
            loginScopes = apiScopes.get(ScopeType.CUSTOMER);
        }

        if (ObjectUtils.isEmpty(loginScopes)) {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "Unable to get login Scopes - Invalid Token");
        }

        return loginScopes;
    }

    private void checkScopes(String jwtToken, LoginScopes[] apiScopes) {
        String userScope = decodeToken(jwtToken).get("scope");

        if (userScope == null)
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "Unable to get the Token Scope - Invalid Token");

        if (StringUtils.equalsIgnoreCase("login", userScope)) {
            userScope = LoginScopes.LOGIN.getValue();
        }

        String tokenScope = userScope;
        if (Stream.of(apiScopes)
                .noneMatch(loginScope ->
                        StringUtils.equalsIgnoreCase(loginScope.getValue(), tokenScope) ||
                                StringUtils.equalsIgnoreCase(LoginScopes.PUBLIC.getValue(), tokenScope))) {
            log.error("User cannot access this API. User scope: {}, API scopes: {}", tokenScope, apiScopes);
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "User does not have privileged to access this API");
        }
    }

    private void checkTokenExpiration(String token) {
        Date today = new Date();

        String tokenExpiredDate = decodeToken(token).get("exp");

        if (tokenExpiredDate == null)
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "Unable to get the Token Expiry Date - Invalid Token");

        Instant instant = Instant.ofEpochSecond(Long.decode(tokenExpiredDate));
        Date expiryDate = Date.from(instant);

        if (expiryDate.before(today)) {
            log.error("User token is expired. Expiry date: {}", expiryDate);
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "Token is expired");
        }
    }

    private void checkSignature(String token) {
        this.checkSignatureFromInternal(token);
    }

    private void checkInitProcessor(){
        initInternalTokenProcessor();
    }

    private void checkSignatureFromInternal(String token) {
        try {
            JWTClaimsSet claimsSet = internalJwtProcessor.process(token, null);
            log.debug("Valid Signature: {}", claimsSet);

        } catch (ParseException | BadJOSEException | JOSEException ex) {
            log.error("Invalid Signature: {}", ex.getMessage());
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "4001", "Invalid token");
        }
    }

    private void initInternalTokenProcessor() {
        try {
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, getInternalKey());
            internalJwtProcessor = new DefaultJWTProcessor<>();

            internalJwtProcessor.setJWSKeySelector(keySelector);

            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .issuer("dans")
                    .build();

            Set<String> properties = new HashSet<>(
                    Arrays.asList("exp", "iat", "aud", "iss", "scope")
            );

            internalJwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(jwtClaimsSet, properties));
        } catch (Exception ex) {
            log.error("Failed init internalProcessor {}", ex.getMessage());
            return;
        }
    }


    private JWKSource<SecurityContext> getInternalKey() {
        if (this.internalKey != null) {
            return this.internalKey;
        }

        this.internalKey = retrieveInternalKey();
        return this.internalKey;
    }

    private synchronized JWKSource<SecurityContext> retrieveInternalKey() {
        RSAKey rsaJWK  = new RSAKey.Builder((RSAPublicKey) keyPair().getPublic())
                .privateKey(keyPair().getPrivate())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(kid)
                .build();
        JWKSet jwkSet = new JWKSet(rsaJWK);
        return new ImmutableJWKSet<>(jwkSet);

    }

    public KeyPair keyPair(){
        PrivateKey privateKey = getPrivateKey(privateKeyValue);
        PublicKey publicKey = getPublicKey(publicKeyValue);
        return new KeyPair(publicKey,privateKey);
    }

}

