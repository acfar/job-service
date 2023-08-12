package com.fauzi.jobservice.apisecurity.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauzi.jobservice.apisecurity.annotation.ApiScope;
import com.fauzi.jobservice.apisecurity.annotation.ApiScopes;
import com.fauzi.jobservice.apisecurity.model.LoginScopes;
import com.fauzi.jobservice.apisecurity.model.ScopeType;
import com.fauzi.jobservice.apisecurity.processor.TokenValidator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Aspect
public class TokenInterceptor {

    private TokenValidator tokenValidator;

    public TokenInterceptor(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Before("within(@org.springframework.web.bind.annotation.RestController *)" +
            "&& @annotation(com.fauzi.jobservice.apisecurity.annotation.ApiScopes)")
    public void validate(JoinPoint joinPoint) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String requestBody;
        try {
            requestBody = convertJsonToString(joinPoint.getArgs());
        } catch (JsonProcessingException e) {
            requestBody = "{}";
        }

        if(StringUtils.equalsIgnoreCase("GET", request.getMethod())){
            requestBody = "";
        }

        Map<ScopeType, LoginScopes[]> apiScopes = this.getApiScope(signature);

        if (apiScopes.isEmpty()) {
            return;
        }

        log.info("API scopes: {}", apiScopes);
        tokenValidator.validateToken(apiScopes, request, requestBody);
    }

    private Map<ScopeType, LoginScopes[]> getApiScope(MethodSignature methodSignature) {
        Method method = methodSignature.getMethod();

        ApiScope[] listApiScope = method.getAnnotation(ApiScopes.class).value();

        return Arrays.stream(listApiScope)
                .collect(Collectors.toMap(ApiScope::type, ApiScope::scope));
    }

    public String convertJsonToString(Object data) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(data);
    }
}


