package com.dropit.delivery.api.application.service;

import com.dropit.delivery.api.domain.model.Delivery;
import com.dropit.delivery.api.domain.model.DeliveryStatus;
import com.dropit.delivery.api.domain.repository.DeliveryRepository;
import com.dropit.delivery.api.domain.repository.TimeslotRepository;
import com.dropit.delivery.api.infrastructure.config.BusinessProperties;
import com.dropit.delivery.api.infrastructure.exception.ConflictException;
import com.dropit.delivery.api.infrastructure.exception.ErrorCode;
import com.dropit.delivery.api.infrastructure.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
public class BookingService implements IBookingService {
	private final DeliveryRepository deliveryRepository;
	private final TimeslotRepository timeslotRepository;
	private final BusinessProperties businessProperties;
	private final Map<String, Semaphore> perSlotSemaphores = new ConcurrentHashMap<>();
	private final Map<LocalDate, Semaphore> perDaySemaphores = new ConcurrentHashMap<>();

	public BookingService(
			DeliveryRepository deliveryRepository, 
			TimeslotRepository timeslotRepository, 
			BusinessProperties businessProperties) {
		this.deliveryRepository = deliveryRepository;
		this.timeslotRepository = timeslotRepository;
		this.businessProperties = businessProperties;
	}

    @Override
	public Delivery book(String user, String timeslotId) {
        var timeslot = timeslotRepository.findById(timeslotId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.UNKNOWN_TIMESLOT, 
                        "unknown timeslot"));

		LocalDate day = timeslot.getStartTime().toLocalDate();
		Semaphore slotSemaphore = perSlotSemaphores.computeIfAbsent(
				timeslotId, 
				id -> new Semaphore(businessProperties.getTimeslotCapacity()));
		Semaphore daySemaphore = perDaySemaphores.computeIfAbsent(
				day, 
				d -> new Semaphore(businessProperties.getDailyCapacity()));

        if (!daySemaphore.tryAcquire()) {
            throw new ConflictException(
                    ErrorCode.DAILY_CAPACITY_REACHED, 
                    "daily capacity reached");
        }

        if (!slotSemaphore.tryAcquire()) {
            daySemaphore.release();
            throw new ConflictException(
                    ErrorCode.TIMESLOT_CAPACITY_REACHED, 
                    "timeslot capacity reached");
        }

        try {
            Delivery delivery = Delivery.builder()
                    .user(user)
                    .timeslotId(timeslotId)
                    .status(DeliveryStatus.PENDING)
                    .build();
            return deliveryRepository.save(delivery);
        } catch (RuntimeException e) {
            slotSemaphore.release();
            daySemaphore.release();
            throw e;
        }
	}

    @Override
	public Delivery complete(String deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.DELIVERY_NOT_FOUND, 
                        "delivery not found"));

        if (delivery.getStatus() == DeliveryStatus.COMPLETED) {
            return delivery;
        }

        delivery.setStatus(DeliveryStatus.COMPLETED);
        return deliveryRepository.save(delivery);
	}

    @Override
	public void cancel(String deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.DELIVERY_NOT_FOUND, 
                        "delivery not found"));

        if (delivery.getStatus() == DeliveryStatus.CANCELLED) {
            return;
        }

        delivery.setStatus(DeliveryStatus.CANCELLED);
        deliveryRepository.save(delivery);

        releaseSemaphores(delivery.getTimeslotId());
	}

	private void releaseSemaphores(String timeslotId) {
        var timeslot = timeslotRepository.findById(timeslotId).orElse(null);
        if (timeslot == null) {
            return;
        }

        Semaphore slotSemaphore = perSlotSemaphores.computeIfAbsent(
                timeslot.getId(), 
                id -> new Semaphore(businessProperties.getTimeslotCapacity()));
        Semaphore daySemaphore = perDaySemaphores.computeIfAbsent(
                timeslot.getStartTime().toLocalDate(), 
                day -> new Semaphore(businessProperties.getDailyCapacity()));

        slotSemaphore.release();
        daySemaphore.release();
	}
}
