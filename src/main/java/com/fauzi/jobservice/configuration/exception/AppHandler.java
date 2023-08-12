package com.fauzi.jobservice.configuration.exception;

import com.fauzi.jobservice.model.response.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
@Log4j2
public class AppHandler {
    private static final String MESSAGE = "{} message {}";

    @ExceptionHandler(GeneralException.class)
    private ResponseEntity<Object> handleBusinessException(GeneralException exception) {
        log.error(
                MESSAGE, exception.getClass().getSimpleName(), ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
                buildResponse(exception.getStatus(), exception.getMessage()), exception.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<Object> handleGeneralException(Exception exception) {
        log.error(
                MESSAGE, exception.getClass().getSimpleName(), ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
                buildResponse("Failed", "Something went wrong in our server, try again later"), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private ErrorResponse buildResponse(String status, String message) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .build();
    }
}
