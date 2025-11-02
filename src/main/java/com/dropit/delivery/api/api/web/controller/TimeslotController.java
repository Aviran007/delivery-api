package com.dropit.delivery.api.api.web.controller;

import com.dropit.delivery.api.api.dto.TimeslotsRequest;
import com.dropit.delivery.api.api.mapper.DtoMapper;
import com.dropit.delivery.api.domain.model.Timeslot;
import com.dropit.delivery.api.application.service.ITimeslotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/timeslots")
@Tag(
	name = "Timeslots",
	description = "Endpoints for querying available delivery timeslots"
)
public class TimeslotController {
	private final ITimeslotService timeslotService;
	private final DtoMapper mapper;

	public TimeslotController(ITimeslotService timeslotService, DtoMapper mapper) {
		this.timeslotService = timeslotService;
		this.mapper = mapper;
	}

	@Operation(
		summary = "Get available timeslots",
		description = """
			Returns all available delivery timeslots for a given address.
			
			**Filtering Rules:**
			- Only timeslots supporting the address country are returned
			- Only timeslots supporting the address postcode are returned (if configured)
			- Only timeslots supporting the address city are returned (if configured)
			- Timeslots on public holidays are excluded (based on country)
			- Past timeslots are included (no time-based filtering)
			
			**Use Case:** After resolving an address, use this endpoint to show available
			delivery windows to the user before booking.
			
			**Returns:** List of timeslot objects with ID, start/end times, and location support info.
			""",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Address to check timeslot availability for",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = TimeslotsRequest.class),
				examples = @ExampleObject(
					name = "Check timeslots",
					value = """
						{
						  "address": {
						    "street": "Rothschild Boulevard",
						    "line1": "12",
						    "line2": "",
						    "country": "IL",
						    "postcode": "6688102",
						    "city": "Tel Aviv"
						  }
						}
						"""
				)
			)
		)
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "List of available timeslots (may be empty if no timeslots match)",
			content = @Content(
				mediaType = "application/json",
				array = @ArraySchema(schema = @Schema(implementation = Object.class)),
				examples = @ExampleObject(
					value = """
						[
						  {
						    "id": "ts-2025-10-30-morning",
						    "startTime": "2025-10-30T09:00:00",
						    "endTime": "2025-10-30T12:00:00",
						    "supportedCountries": ["IL", "US"],
						    "supportedPostcodes": ["6688102", "6688103"],
						    "supportedCities": ["Tel Aviv", "Ramat Gan"]
						  },
						  {
						    "id": "ts-2025-10-30-afternoon",
						    "startTime": "2025-10-30T14:00:00",
						    "endTime": "2025-10-30T18:00:00",
						    "supportedCountries": ["IL"],
						    "supportedPostcodes": ["6688102"],
						    "supportedCities": ["Tel Aviv"]
						  }
						]
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid request (validation error)",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						  "timestamp": "2025-10-30T14:23:45.123Z",
						  "status": 400,
						  "errors": {
						    "address": "Address is required",
						    "address.country": "Country is required"
						  }
						}
						"""
				)
			)
		)
	})
	@PostMapping
	public List<Object> getAvailableTimeslots(@Valid @RequestBody TimeslotsRequest request) {
	
		List<Timeslot> timeslots = timeslotService.availableTimeslots(request.getAddress());
		
		if (timeslots == null) {
			return List.of();
		}

		return timeslots.stream()
				.map(mapper::toTimeslotMap)
				.collect(Collectors.toList());
	}
}

