package com.dropit.delivery.api.application.service;

import com.dropit.delivery.api.infrastructure.client.HolidayClient;
import com.dropit.delivery.api.api.dto.AddressDTO;
import com.dropit.delivery.api.domain.model.Timeslot;
import com.dropit.delivery.api.domain.repository.TimeslotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TimeslotService implements ITimeslotService {
	private static final Logger logger = LoggerFactory.getLogger(TimeslotService.class);
	private final TimeslotRepository timeslotRepository;
	private final HolidayClient holidayClient;

	public TimeslotService(TimeslotRepository timeslotRepository, HolidayClient holidayClient) {
		this.timeslotRepository = timeslotRepository;
		this.holidayClient = holidayClient;
	}

	@Override
	public List<Timeslot> availableTimeslots(AddressDTO address) {
		if (address == null) {
			return List.of();
		}

		// Fetch holidays and timeslots in PARALLEL using CompletableFuture
		CompletableFuture<Set<LocalDate>> holidaysFuture = CompletableFuture.supplyAsync(() -> {
			logger.debug("Fetching holidays for country: {} in parallel", address.getCountry());
			return holidayClient.holidaysForYear(address.getCountry(), LocalDate.now().getYear());
		});

		CompletableFuture<List<Timeslot>> timeslotsFuture = CompletableFuture.supplyAsync(() -> {
			logger.debug("Fetching timeslots from repository in parallel");
			return List.copyOf(timeslotRepository.findAll());
		});

		// Wait for both to complete and combine results
		try {
			Set<LocalDate> holidays = holidaysFuture.join();
			List<Timeslot> allTimeslots = timeslotsFuture.join();

			logger.debug("Parallel fetch completed. Holidays: {}, Timeslots: {}", holidays.size(), allTimeslots.size());

			return allTimeslots.stream()
					.filter(ts -> supportsAddress(ts, address))
					.filter(ts -> !isHoliday(ts, holidays))
					.collect(Collectors.toList());
		} catch (Exception e) {
			logger.error("Error during parallel timeslot fetch", e);
			return List.of();
		}
	}

	private boolean isHoliday(Timeslot timeslot, Set<LocalDate> holidays) {
		return holidays.contains(timeslot.getStartTime().toLocalDate());
	}

	private boolean supportsAddress(Timeslot timeslot, AddressDTO address) {
		return supportsCountry(timeslot, address.getCountry())
				&& supportsPostcode(timeslot, address.getPostcode())
				&& supportsCity(timeslot, address.getCity());
	}

	private boolean supportsCountry(Timeslot timeslot, String country) {
		Set<String> supportedCountries = timeslot.getSupportedCountries();
		return supportedCountries.isEmpty() || supportedCountries.contains(country);
	}

	private boolean supportsPostcode(Timeslot timeslot, String postcode) {
		Set<String> supportedPostcodes = timeslot.getSupportedPostcodes();
		return supportedPostcodes.isEmpty() || supportedPostcodes.contains(postcode);
	}

	private boolean supportsCity(Timeslot timeslot, String city) {
		Set<String> supportedCities = timeslot.getSupportedCities();
		return supportedCities.isEmpty() || supportedCities.contains(city);
	}
}
