package com.dropit.delivery.api.application.service;

import com.dropit.delivery.api.domain.model.Delivery;

public interface IBookingService {
	Delivery book(String user, String timeslotId);
	Delivery complete(String deliveryId);
	void cancel(String deliveryId);
}

