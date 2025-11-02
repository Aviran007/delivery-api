package com.dropit.delivery.api.api.web.handler;

import com.dropit.delivery.api.infrastructure.exception.ApiException;
import com.dropit.delivery.api.infrastructure.exception.ConflictException;
import com.dropit.delivery.api.infrastructure.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        logger.warn("Not Found: {} - {}", ex.getError(), ex.getMessage());
        Map<String, Object> body = createApiExceptionBody(ex, HttpStatus.NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        logger.warn("Conflict: {} - {}", ex.getError(), ex.getMessage());
        Map<String, Object> body = createApiExceptionBody(ex, HttpStatus.CONFLICT);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

	private Map<String, Object> createApiExceptionBody(ApiException ex, HttpStatus status) {
		Map<String, Object> body = createBaseErrorBody(status);
		body.put("message", ex.getMessage());
		body.put("error", ex.getError().name());
		return body;
	}
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		logger.warn("Validation error: {}", ex.getMessage());
		Map<String, Object> body = createBaseErrorBody(HttpStatus.BAD_REQUEST);
		body.put("errors", extractValidationErrors(ex));
		return ResponseEntity.badRequest().body(body);
	}

	private Map<String, String> extractValidationErrors(MethodArgumentNotValidException ex) {
		return ex.getBindingResult().getAllErrors().stream()
				.collect(Collectors.toMap(
						error -> error instanceof FieldError 
								? ((FieldError) error).getField() 
								: error.getObjectName(),
						error -> error.getDefaultMessage() != null 
								? error.getDefaultMessage() 
								: "Invalid value"
				));
	}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        Map<String, Object> body = createBaseErrorBody(HttpStatus.BAD_REQUEST);
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleUnexpectedError(Exception ex) {
		logger.error("Unexpected error occurred", ex);
		Map<String, Object> body = createBaseErrorBody(HttpStatus.INTERNAL_SERVER_ERROR);
		body.put("message", "An unexpected error occurred");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	private Map<String, Object> createBaseErrorBody(HttpStatus status) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		return body;
	}
}
