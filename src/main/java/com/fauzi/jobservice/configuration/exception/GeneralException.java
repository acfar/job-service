package com.fauzi.jobservice.configuration.exception;

import org.springframework.http.HttpStatus;

public class GeneralException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String status;
    private final String message;

    public GeneralException(HttpStatus httpStatus, String status, String message) {
        this.httpStatus = httpStatus;
        this.status = status;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
