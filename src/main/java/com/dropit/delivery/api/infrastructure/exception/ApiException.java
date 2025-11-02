package com.dropit.delivery.api.infrastructure.exception;

public class ApiException extends RuntimeException {
	private final ErrorCode error;

	public ApiException(ErrorCode error, String message) { super(message); this.error = error; }
	public ErrorCode getError() { return error; }
}
