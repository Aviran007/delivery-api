package com.dropit.delivery.api.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to fetch available timeslots for a specific address")
public class TimeslotsRequest {
	
	@Schema(
		description = "Structured address to check available timeslots for",
		required = true,
		implementation = AddressDTO.class
	)
	@Valid
	@NotNull(message = "Address is required")
	private AddressDTO address;

	public AddressDTO getAddress() { return address; }
	public void setAddress(AddressDTO address) { this.address = address; }
}
