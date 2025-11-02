package com.dropit.delivery.api.service;

import com.dropit.delivery.api.infrastructure.client.HolidayClient;
import com.dropit.delivery.api.api.dto.AddressDTO;
import com.dropit.delivery.api.domain.model.Timeslot;
import com.dropit.delivery.api.domain.repository.TimeslotRepository;
import com.dropit.delivery.api.application.service.TimeslotService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TimeslotServiceTest {
	@Test
	void filtersBySupportedAddressAndSkipsHolidays() {
		TimeslotRepository repo = new TimeslotRepository();
		LocalDateTime day = LocalDate.now().withDayOfMonth(LocalDate.now().getDayOfMonth()).atTime(9, 0);
		repo.saveAll(List.of(
				new Timeslot("A", day, day.plusHours(3), Set.of("IL"), Set.of(), Set.of("Tel Aviv")),
				new Timeslot("B", day.plusDays(1), day.plusDays(1).plusHours(3), Set.of("US"), Set.of(), Set.of("NYC"))
		));

		HolidayClient holidays = new HolidayClient("", "") {
			@Override
			public Set<LocalDate> holidaysForYear(String country, int year) { return Set.of(day.toLocalDate()); }
		};

		TimeslotService svc = new TimeslotService(repo, holidays);
		AddressDTO address = new AddressDTO();
		address.setCountry("IL");
		address.setCity("Tel Aviv");
		address.setPostcode("");

		var result = svc.availableTimeslots(address);
		assertTrue(result.stream().noneMatch(ts -> ts.getId().equals("A")), "Holiday date should be filtered out");
		assertTrue(result.stream().noneMatch(ts -> ts.getId().equals("B")), "Unsupported country should be filtered out");
	}
}
