package com.browserfilesystem.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@RestControllerAdvice
/** Converts exceptions raised by controllers and services into predictable JSON HTTP error responses. */
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    /** Preserves explicit domain statuses such as 404 parent missing or 409 duplicate name. */
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return error(status, exception.getReason());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    /** Reports invalid validated request parameters as a client error. */
    public ResponseEntity<ApiError> handleValidation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .findFirst()
                .orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /** Reports invalid JSON request fields using the first validation message. */
    public ResponseEntity<ApiError> handleRequestBodyValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    /** Handles values that cannot be converted to the controller parameter type. */
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, "Invalid value for " + exception.getName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    /** Handles required query parameters omitted by an API client. */
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException exception) {
        return error(HttpStatus.BAD_REQUEST, "Missing required parameter " + exception.getParameterName());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    /** Provides a domain-friendly response if MongoDB catches a uniqueness race condition. */
    public ResponseEntity<ApiError> handleDuplicateKey(DuplicateKeyException exception) {
        return error(HttpStatus.CONFLICT, "An item with this name already exists in the folder");
    }

    @ExceptionHandler(Exception.class)
    /** Logs internal details while keeping the client-facing 500 message safe and generic. */
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        LOGGER.error("Unhandled server error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    /** Builds the common error response envelope for all handlers above. */
    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status).body(body);
    }
}
