package com.dropit.delivery.api.service;

import com.dropit.delivery.api.infrastructure.config.BusinessProperties;
import com.dropit.delivery.api.domain.model.Timeslot;
import com.dropit.delivery.api.domain.repository.DeliveryRepository;
import com.dropit.delivery.api.domain.repository.TimeslotRepository;
import com.dropit.delivery.api.application.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceConcurrencyTest {
	private BookingService bookingService;
	private TimeslotRepository timeslotRepository;
	private DeliveryRepository deliveryRepository;

	@BeforeEach
	void setup() {
		this.timeslotRepository = new TimeslotRepository();
		this.deliveryRepository = new DeliveryRepository();
		this.timeslotRepository.saveAll(List.of(
				new Timeslot("A", LocalDateTime.now().withHour(9).withMinute(0), LocalDateTime.now().withHour(12).withMinute(0), Set.of("IL"), Set.of(), Set.of("Tel Aviv"))
		));
		this.bookingService = new BookingService(deliveryRepository, timeslotRepository, new BusinessProperties(10, 2));
	}

	@Test
	void enforcesTimeslotCapacity() throws InterruptedException {
		int threads = 5;
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);
		CopyOnWriteArrayList<Boolean> results = new CopyOnWriteArrayList<>();
		for (int i = 0; i < threads; i++) {
			int idx = i;
			new Thread(() -> {
				try {
					start.await();
					bookingService.book("user" + idx, "A");
					results.add(true);
				} catch (Exception e) {
					results.add(false);
				} finally {
					done.countDown();
				}
			}).start();
		}
		start.countDown();
		done.await();
		long successes = results.stream().filter(Boolean::booleanValue).count();
		assertEquals(2, successes);
	}
}
