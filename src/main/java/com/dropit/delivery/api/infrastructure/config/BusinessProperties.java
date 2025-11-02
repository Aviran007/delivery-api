package com.dropit.delivery.api.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BusinessProperties {
	private final int dailyCapacity;
	private final int timeslotCapacity;

	public BusinessProperties(
			@Value("${business.daily.capacity}") int dailyCapacity,
			@Value("${business.timeslot.capacity}") int timeslotCapacity) {
		this.dailyCapacity = dailyCapacity;
		this.timeslotCapacity = timeslotCapacity;
	}
	public int getDailyCapacity() { return dailyCapacity; }
	public int getTimeslotCapacity() { return timeslotCapacity; }
}
