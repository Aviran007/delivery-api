package com.dropit.delivery.api.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to book a delivery in a specific timeslot")
public class BookDeliveryRequest {
	
	@Schema(
		description = "User identifier (email or username)",
		example = "john.doe@example.com",
		required = true
	)
	@NotBlank(message = "User is required")
	private String user;
	
	@Schema(
		description = "ID of the timeslot to book",
		example = "ts-2025-10-30-morning",
		required = true
	)
	@NotBlank(message = "Timeslot ID is required")
	private String timeslotId;

	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }
	public String getTimeslotId() { return timeslotId; }
	public void setTimeslotId(String timeslotId) { this.timeslotId = timeslotId; }
}
