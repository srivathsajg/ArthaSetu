package com.arthasetu.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle IllegalArgumentException.
     *
     * Used for business validation errors such as:
     * - Insufficient balance
     * - Account not found
     * - Unauthorized account access
     * - Inactive account
     * - Same source and destination account
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                exception.getMessage(),
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle @Valid request body validation errors.
     *
     * Example:
     * amount = null
     * accountNumber = empty
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> validationErrors.put(
                        error.getField(),
                        error.getDefaultMessage()));

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                validationErrors.toString(),
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle validation errors from method parameters.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                exception.getMessage(),
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle all unexpected exceptions.
     *
     * We don't expose the internal exception or stack trace
     * to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception exception,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Standard API error response.
     */
    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path) {
    }
}