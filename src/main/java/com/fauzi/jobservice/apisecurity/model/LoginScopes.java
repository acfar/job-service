package com.fauzi.jobservice.apisecurity.model;

import lombok.Getter;

@Getter
public enum LoginScopes {
    LOGIN("login"),
    PUBLIC("public");

    private final String value;

    LoginScopes(String value){this.value = value;}
}

