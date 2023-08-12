package com.fauzi.jobservice.apisecurity.annotation;


import com.fauzi.jobservice.apisecurity.model.LoginScopes;
import com.fauzi.jobservice.apisecurity.model.ScopeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiScope {
    ScopeType type();
    LoginScopes[] scope();
}

