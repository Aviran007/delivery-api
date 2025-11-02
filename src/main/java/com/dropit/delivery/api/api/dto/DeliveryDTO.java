package com.dropit.delivery.api.api.dto;

import com.dropit.delivery.api.domain.model.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Delivery information response")
public class DeliveryDTO {
	
	@Schema(
		description = "Unique delivery identifier",
		example = "d1f8e9a3-4b2c-4d5e-8f6a-7b8c9d0e1f2a"
	)
	private String id;
	
	@Schema(
		description = "User who booked the delivery",
		example = "john.doe@example.com"
	)
	private String user;
	
	@Schema(
		description = "Associated timeslot ID",
		example = "ts-2025-10-30-morning"
	)
	private String timeslotId;
	
	@Schema(
		description = "Current delivery status",
		example = "PENDING",
		allowableValues = {"PENDING", "COMPLETED", "CANCELLED"}
	)
	private DeliveryStatus status;

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }
	public String getTimeslotId() { return timeslotId; }
	public void setTimeslotId(String timeslotId) { this.timeslotId = timeslotId; }
	public DeliveryStatus getStatus() { return status; }
	public void setStatus(DeliveryStatus status) { this.status = status; }
}
