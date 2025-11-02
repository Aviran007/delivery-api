package com.dropit.delivery.api.service;

import com.dropit.delivery.api.infrastructure.config.BusinessProperties;
import com.dropit.delivery.api.infrastructure.exception.ConflictException;
import com.dropit.delivery.api.domain.model.Timeslot;
import com.dropit.delivery.api.domain.repository.DeliveryRepository;
import com.dropit.delivery.api.domain.repository.TimeslotRepository;
import com.dropit.delivery.api.application.service.BookingService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceParameterizedTest {
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3})
	void bookingSucceedsUpToTimeslotCapacity(int capacity) {
		TimeslotRepository tsRepo = new TimeslotRepository();
		DeliveryRepository dRepo = new DeliveryRepository();
		tsRepo.saveAll(List.of(
				new Timeslot("X", LocalDateTime.now().withHour(9).withMinute(0), LocalDateTime.now().withHour(12).withMinute(0), Set.of("IL"), Set.of(), Set.of("Tel Aviv"))
		));
		BookingService svc = new BookingService(dRepo, tsRepo, new BusinessProperties(100, capacity));

		for (int i = 0; i < capacity; i++) {
			final int index = i;
			assertDoesNotThrow(() -> svc.book("u" + index, "X"));
		}
		assertThrows(ConflictException.class, () -> svc.book("overflow", "X"));
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2})
	void cancellationReleasesTimeslotAndDayCapacity(int capacity) {
		TimeslotRepository tsRepo = new TimeslotRepository();
		DeliveryRepository dRepo = new DeliveryRepository();
		tsRepo.saveAll(List.of(
				new Timeslot("Y", LocalDateTime.now().withHour(9).withMinute(0), LocalDateTime.now().withHour(12).withMinute(0), Set.of("IL"), Set.of(), Set.of("Tel Aviv"))
		));
		BookingService svc = new BookingService(dRepo, tsRepo, new BusinessProperties(capacity, capacity));

		String lastId = null;
		for (int i = 0; i < capacity; i++) {
			final int index = i;
			lastId = svc.book("u" + index, "Y").getId();
		}
		assertThrows(ConflictException.class, () -> svc.book("overflow", "Y"));

		// cancel one then booking should succeed again
		svc.cancel(lastId);
		assertDoesNotThrow(() -> svc.book("new", "Y"));
	}
}
