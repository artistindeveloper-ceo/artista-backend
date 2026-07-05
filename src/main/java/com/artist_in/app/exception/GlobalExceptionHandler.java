package com.artist_in.app.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.artist_in.app.dto.common.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, null);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request, null);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username/email or password.", request, null);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.", request, null);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex,
			HttpServletRequest request) {
		return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "Uploaded file exceeds the maximum allowed size.", request,
				null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
		return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields.", request, fieldErrors);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
		ex.printStackTrace(); // ya log.error("Transpose error", ex); agar Slf4j hai
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.",
				request, null);
	}

	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request,
			Map<String, String> fieldErrors) {
		ErrorResponse error = ErrorResponse.builder().timestamp(Instant.now()).status(status.value())
				.error(status.getReasonPhrase()).message(message).path(request.getRequestURI()).fieldErrors(fieldErrors)
				.build();
		return ResponseEntity.status(status).body(error);
	}
}
