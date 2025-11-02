package com.dropit.delivery.api.domain.repository;

import com.dropit.delivery.api.domain.model.Delivery;
import com.dropit.delivery.api.domain.model.DeliveryStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class DeliveryRepository implements BaseRepository<Delivery, String> {
	private final Map<String, Delivery> idToDelivery = new ConcurrentHashMap<>();

	@Override
	public Delivery save(Delivery delivery) {
		validateDelivery(delivery);
		idToDelivery.put(delivery.getId(), delivery);
		return delivery;
	}

	@Override
	public Optional<Delivery> findById(String id) {
		if (isInvalidId(id)) {
			return Optional.empty();
		}
		return Optional.ofNullable(idToDelivery.get(id));
	}

	@Override
	public Collection<Delivery> findAll() {
		return idToDelivery.values();
	}

	@Override
	public void delete(String id) {
		if (isInvalidId(id)) {
			return;
		}
		idToDelivery.remove(id);
	}

	private void validateDelivery(Delivery delivery) {
		if (delivery == null) {
			throw new IllegalArgumentException("Delivery cannot be null");
		}
		if (isInvalidId(delivery.getId())) {
			throw new IllegalArgumentException("Delivery ID cannot be null or empty");
		}
	}

	private boolean isInvalidId(String id) {
		return id == null || id.trim().isEmpty();
	}

	public List<Delivery> findByDate(LocalDate date) {
		if (date == null) {
			return List.of();
		}
		return idToDelivery.values().stream()
				.filter(delivery -> isCreatedOnDate(delivery, date))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public List<Delivery> findByWeek(LocalDate anyDayInWeek) {
		if (anyDayInWeek == null) {
			return List.of();
		}
		
		LocalDate weekStart = anyDayInWeek.with(java.time.DayOfWeek.MONDAY);
		LocalDate weekEnd = weekStart.plusDays(6);
		
		return idToDelivery.values().stream()
				.filter(delivery -> isCreatedInWeek(delivery, weekStart, weekEnd))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private boolean isCreatedOnDate(Delivery delivery, LocalDate date) {
		return delivery.getCreatedAt() != null 
				&& delivery.getCreatedAt().toLocalDate().isEqual(date);
	}

	private boolean isCreatedInWeek(Delivery delivery, LocalDate start, LocalDate end) {
		if (delivery.getCreatedAt() == null) {
			return false;
		}
		LocalDate createdDate = delivery.getCreatedAt().toLocalDate();
		return !createdDate.isBefore(start) && !createdDate.isAfter(end);
	}

	public long countByDate(LocalDate date) {
		if (date == null) {
			return 0;
		}
		return idToDelivery.values().stream()
				.filter(delivery -> isActiveOnDate(delivery, date))
				.count();
	}

	private boolean isActiveOnDate(Delivery delivery, LocalDate date) {
		return delivery.getCreatedAt() != null
				&& delivery.getCreatedAt().toLocalDate().isEqual(date)
				&& delivery.getStatus() != DeliveryStatus.CANCELLED;
	}
}

