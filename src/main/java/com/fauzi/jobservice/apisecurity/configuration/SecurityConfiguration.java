package com.fauzi.jobservice.apisecurity.configuration;

import com.fauzi.jobservice.apisecurity.interceptor.TokenInterceptor;
import com.fauzi.jobservice.apisecurity.processor.TokenValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfiguration {

    @Bean
    public TokenValidator tokenValidator(){
        return new TokenValidator();
    }

    @Bean
    public TokenInterceptor tokenInterceptor(TokenValidator tokenValidator){
        return new TokenInterceptor(tokenValidator);
    }
}

