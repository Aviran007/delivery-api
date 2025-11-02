package com.dropit.delivery.api.domain.repository;

import com.dropit.delivery.api.domain.model.Timeslot;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TimeslotRepository implements BaseRepository<Timeslot, String> {
	private final Map<String, Timeslot> idToTimeslot = new ConcurrentHashMap<>();

	public void saveAll(Collection<Timeslot> timeslots) {
		if (timeslots == null) {
			return;
		}
		timeslots.forEach(this::saveIfValid);
	}

	@Override
	public Timeslot save(Timeslot timeslot) {
		if (timeslot != null && timeslot.getId() != null) {
			idToTimeslot.put(timeslot.getId(), timeslot);
			return timeslot;
		}
		throw new IllegalArgumentException("Timeslot or ID cannot be null");
	}

	@Override
	public Optional<Timeslot> findById(String id) {
		if (isInvalidId(id)) {
			return Optional.empty();
		}
		return Optional.ofNullable(idToTimeslot.get(id));
	}

	@Override
	public Collection<Timeslot> findAll() {
		return idToTimeslot.values();
	}

	@Override
	public void delete(String id) {
		if (!isInvalidId(id)) {
			idToTimeslot.remove(id);
		}
	}

	private void saveIfValid(Timeslot timeslot) {
		if (timeslot != null && timeslot.getId() != null) {
			idToTimeslot.put(timeslot.getId(), timeslot);
		}
	}

	private boolean isInvalidId(String id) {
		return id == null || id.trim().isEmpty();
	}

	public void clear() {
		idToTimeslot.clear();
	}
}

