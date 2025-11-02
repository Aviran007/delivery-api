package com.dropit.delivery.api.infrastructure.exception;

public class NotFoundException extends ApiException {
	public NotFoundException(ErrorCode error, String message) { super(error, message); }
}
