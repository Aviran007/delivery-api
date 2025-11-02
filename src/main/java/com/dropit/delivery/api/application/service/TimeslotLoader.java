package com.dropit.delivery.api.application.service;

import com.dropit.delivery.api.domain.model.Timeslot;
import com.dropit.delivery.api.domain.repository.TimeslotRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class TimeslotLoader {
	private static final Logger logger = LoggerFactory.getLogger(TimeslotLoader.class);
	private final TimeslotRepository repository;
	private final ObjectMapper mapper = new ObjectMapper();

	public TimeslotLoader(TimeslotRepository repository) { this.repository = repository; }

	@PostConstruct
	public void load() {
		try {
			loadTimeslotsFromResource();
		} catch (Exception e) {
			handleLoadError(e);
		}
	}

	private void loadTimeslotsFromResource() throws Exception {
		InputStream inputStream = new ClassPathResource("courier_timeslots.json").getInputStream();
		
		if (inputStream == null) {
			throw new IllegalStateException("courier_timeslots.json not found");
		}

		JsonNode root = mapper.readTree(inputStream);
		
		if (!root.isArray()) {
			throw new IllegalStateException("JSON root must be an array");
		}

		List<Timeslot> timeslots = parseTimeslots(root);
		
		if (timeslots.isEmpty()) {
			throw new IllegalStateException("No valid timeslots found");
		}

		repository.saveAll(timeslots);
	}

	private List<Timeslot> parseTimeslots(JsonNode root) {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		
		return StreamSupport.stream(root.spliterator(), false)
				.map(node -> parseTimeslot(node, formatter))
				.filter(timeslot -> timeslot != null)
				.collect(Collectors.toList());
	}

	private Timeslot parseTimeslot(JsonNode node, DateTimeFormatter formatter) {
		try {
			String id = node.path("id").asText();
			LocalDateTime start = LocalDateTime.parse(node.path("startTime").asText(), formatter);
			LocalDateTime end = LocalDateTime.parse(node.path("endTime").asText(), formatter);
			
			return new Timeslot(
					id,
					start,
					end,
					toSet(node.path("supportedCountries")),
					toSet(node.path("supportedPostcodes")),
					toSet(node.path("supportedCities")));
		} catch (Exception e) {
			logger.warn("Failed to parse timeslot", e);
			return null;
		}
	}

	private void handleLoadError(Exception e) {
		logger.error("Error loading timeslots", e);
		repository.clear();
	}

	private Set<String> toSet(JsonNode node) {
		if (node == null || !node.isArray()) {
			return Set.of();
		}
		
		return StreamSupport.stream(node.spliterator(), false)
				.map(JsonNode::asText)
				.collect(Collectors.toSet());
	}
}
