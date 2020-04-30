package com.jumbo.routeplanner.gateway.controller.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbo.routeplanner.domain.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GenericExceptionHandler {

    private final ObjectMapper mapper;

    @ExceptionHandler(Throwable.class)
    public ResponseEntity handleException(Throwable e) {
        return handle(e, INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity handleValidationException(MethodArgumentTypeMismatchException e) {
        return handle(e, BAD_REQUEST, e.getName());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity handleValidationException(ValidationException e) {
        return handle(e, BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return handle(e, BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity handleBindException(BindException e) {
        FieldError fieldError = e.getFieldError();

        String message = "bad request";

        if (fieldError != null) {
            message = String.format("%s is not a valid value for %s, %s",
                    fieldError.getRejectedValue(),
                    fieldError.getField(),
                    fieldError.getDefaultMessage());
        }
        return handle(e, BAD_REQUEST, message);
    }

    private ResponseEntity handle(Throwable e, HttpStatus status, String message) {
        log.error(e.getMessage(), e);

        String json;
        try {
            json = mapper.writeValueAsString(message);
        } catch (JsonProcessingException e1) {
            json = "\"error\"";
        }

        return ResponseEntity
                .status(status)
                .header("Content-Type", "application/json")
                .body(json);
    }
}
