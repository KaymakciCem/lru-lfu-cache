package com.celonis.kvstore.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.celonis.kvstore.lfucache.LFUKVStore;

@ControllerAdvice
public class GlobalErrorHandler {
    private static Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleRequestBodyError(MethodArgumentNotValidException ex){
        logger.error("Exception caught in handleRequestBodyError :  {} " ,ex.getMessage(),  ex);
        String error = ex.getBindingResult()
                           .getAllErrors()
                           .stream()
                           .map(DefaultMessageSourceResolvable::getDefaultMessage)
                           .sorted()
                           .collect(Collectors.joining(","));

        logger.error("errorList : {}", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CacheTypeNotFoundException.class)
    public ResponseEntity<String> handleCacheTypeNotfoundException(CacheTypeNotFoundException ex){
        logger.error("Exception caught in handleCacheTypeNotfoundException :  {} " ,ex.getMessage(),  ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> globalExceptionHandler(Exception ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
