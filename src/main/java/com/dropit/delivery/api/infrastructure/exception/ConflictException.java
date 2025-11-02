package com.dropit.delivery.api.infrastructure.exception;

public class ConflictException extends ApiException {
	public ConflictException(ErrorCode error, String message) { super(error, message); }
}
