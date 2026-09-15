package com.fitnexus.backend.exception;

import com.fitnexus.backend.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across the application
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("Validation error: {}", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        LocalDateTime.now().toString(),
                        request.getDescription(false).replace("uri=", ""),
                        fieldErrors
                ));
    }

    /**
     * Handle IllegalArgumentException (bad input data)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        log.warn("Illegal argument: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        LocalDateTime.now().toString(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Handle SecurityException (authorization errors)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurityException(
            SecurityException ex,
            WebRequest request) {

        log.warn("Security exception: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        ex.getMessage(),
                        LocalDateTime.now().toString(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Handle EntityNotFoundException (resource not found)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(
            EntityNotFoundException ex,
            WebRequest request) {

        log.warn("Entity not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        LocalDateTime.now().toString(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Handle DuplicateResourceException (duplicate email, etc.)
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicateResourceException(
            DuplicateResourceException ex,
            WebRequest request) {

        log.warn("Duplicate resource: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage(),
                        LocalDateTime.now().toString(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Handle UnauthorizedException (authentication errors)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(
            UnauthorizedException ex,
            WebRequest request) {

        log.warn("Unauthorized access: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        ex.getMessage(),
                        LocalDateTime.now().toString(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Handle InvalidOperationException (business logic violations)
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<?> handleInvalidOperationException(
            InvalidOperationException ex,
            WebRequest request) {

        log.warn("Invalid operation: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        LocalDateTime.now().toString(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Handle upload files that exceed configured limits
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            WebRequest request) {

        log.warn("Upload too large: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatusCode.valueOf(413))
                .body(new ApiErrorResponse(
                        413,
                        "The uploaded file is too large. Please choose a file smaller than 10 MB.",
                        LocalDateTime.now().toString(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Handle 404 Not Found for endpoints
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            WebRequest request) {

        log.warn("Handler not found: {}", ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Endpoint not found",
                        LocalDateTime.now().toString(),
                        ex.getRequestURL()
                ));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred: " + ex.getMessage() + " | " + ex.getClass().getName() + " | Cause: " + (ex.getCause() != null ? ex.getCause().getMessage() : "none"),
                        LocalDateTime.now().toString(),
                        request.getDescription(false).replace("uri=", "")
                ));
    }

    /**
     * Validation error response with field-level errors
     */
    public static class ValidationErrorResponse extends ApiErrorResponse {
        private final Map<String, String> fieldErrors;

        public ValidationErrorResponse(int status, String message, String timestamp, String path, Map<String, String> fieldErrors) {
            super(status, message, timestamp, path);
            this.fieldErrors = fieldErrors;
        }

        public Map<String, String> getFieldErrors() {
            return fieldErrors;
        }
    }
}

