package com.be16_2nd.SmartFridge.common.service;

import com.be16_2nd.SmartFridge.common.dto.CommonErrorDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illergalException(IllegalArgumentException e){
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> noSuchException(NoSuchElementException e){
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND.value(), e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error(e.getMessage());
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNotFoundException(EntityNotFoundException e){
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND.value(), e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthorizationServiceException.class)
    public ResponseEntity<?> authorizationServiceException(AuthorizationServiceException e){
        log.error(e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED.value(), e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exception(Exception e){
        log.error(e.getMessage());
        String errorMessage = e.getMessage();
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
