package com.dropit.delivery.api.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Delivery booking item with Builder pattern for flexible construction.
 */
public final class Delivery {
	private final String id;
	private final String user;
	private final String timeslotId;
	private final LocalDateTime createdAt;
	private DeliveryStatus status;

	private Delivery(Builder builder) {
		this.id = builder.id;
		this.user = builder.user;
		this.timeslotId = builder.timeslotId;
		this.status = builder.status;
		this.createdAt = builder.createdAt;
	}

	public String getId() { return id; }
	public String getUser() { return user; }
	public String getTimeslotId() { return timeslotId; }
	public DeliveryStatus getStatus() { return status; }
	public void setStatus(DeliveryStatus status) { this.status = status; }
	public LocalDateTime getCreatedAt() { return createdAt; }

	@Override public boolean equals(Object o) { return o instanceof Delivery && Objects.equals(id, ((Delivery) o).id); }
	@Override public int hashCode() { return Objects.hash(id); }

	public static class Builder {
		private String id;
		private String user;
		private String timeslotId;
		private DeliveryStatus status = DeliveryStatus.PENDING;
		private LocalDateTime createdAt = LocalDateTime.now();

		public Builder() {
			// Generate unique ID by default
			this.id = UUID.randomUUID().toString();
		}

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder user(String user) {
			if (user == null || user.trim().isEmpty()) {
				throw new IllegalArgumentException("User cannot be null or empty");
			}
			this.user = user;
			return this;
		}

		public Builder timeslotId(String timeslotId) {
			if (timeslotId == null || timeslotId.trim().isEmpty()) {
				throw new IllegalArgumentException("Timeslot ID cannot be null or empty");
			}
			this.timeslotId = timeslotId;
			return this;
		}

		public Builder status(DeliveryStatus status) {
			if (status == null) {
				throw new IllegalArgumentException("Status cannot be null");
			}
			this.status = status;
			return this;
		}

		public Builder createdAt(LocalDateTime createdAt) {
			if (createdAt == null) {
				throw new IllegalArgumentException("Created at cannot be null");
			}
			this.createdAt = createdAt;
			return this;
		}

		/**
		 * Builds the Delivery instance with validation.
		 * @return A validated Delivery instance
		 * @throws IllegalStateException if required fields are missing
		 */
		public Delivery build() {
			if (user == null) {
				throw new IllegalStateException("User is required");
			}
			if (timeslotId == null) {
				throw new IllegalStateException("Timeslot ID is required");
			}
			
			return new Delivery(this);
		}

	
		public static Builder from(Delivery delivery) {
			return new Builder()
					.id(delivery.id)
					.user(delivery.user)
					.timeslotId(delivery.timeslotId)
					.status(delivery.status)
					.createdAt(delivery.createdAt);
		}
	}


	public static Builder builder() {
		return new Builder();
	}


	public Delivery withStatus(DeliveryStatus newStatus) {
		return Builder.from(this).status(newStatus).build();
	}
}

