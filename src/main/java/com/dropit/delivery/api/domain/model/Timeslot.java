package com.dropit.delivery.api.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Delivery window with supported address attributes.
 */
public final class Timeslot {
	private final String id;
	private final LocalDateTime startTime;
	private final LocalDateTime endTime;
	private final Set<String> supportedCountries;
	private final Set<String> supportedPostcodes;
	private final Set<String> supportedCities;

	public Timeslot(String id, LocalDateTime startTime, LocalDateTime endTime, Set<String> supportedCountries, Set<String> supportedPostcodes, Set<String> supportedCities) {
		this.id = id;
		this.startTime = startTime;
		this.endTime = endTime;
		this.supportedCountries = supportedCountries;
		this.supportedPostcodes = supportedPostcodes;
		this.supportedCities = supportedCities;
	}

	public String getId() { return id; }
	public LocalDateTime getStartTime() { return startTime; }
	public LocalDateTime getEndTime() { return endTime; }
	public Set<String> getSupportedCountries() { return supportedCountries; }
	public Set<String> getSupportedPostcodes() { return supportedPostcodes; }
	public Set<String> getSupportedCities() { return supportedCities; }
}

