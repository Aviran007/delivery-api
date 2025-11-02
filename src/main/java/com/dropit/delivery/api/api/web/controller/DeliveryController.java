package com.dropit.delivery.api.api.web.controller;

import com.dropit.delivery.api.api.dto.BookDeliveryRequest;
import com.dropit.delivery.api.api.dto.DeliveryDTO;
import com.dropit.delivery.api.api.mapper.DtoMapper;
import com.dropit.delivery.api.domain.model.Delivery;
import com.dropit.delivery.api.application.service.IBookingService;
import com.dropit.delivery.api.application.service.IQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/deliveries")
@Tag(
	name = "Deliveries",
	description = "Endpoints for managing delivery bookings, completions, and cancellations"
)
public class DeliveryController {
	private final IBookingService bookingService;
	private final IQueryService queryService;
	private final DtoMapper mapper;

	public DeliveryController(IBookingService bookingService, IQueryService queryService, DtoMapper mapper) {
		this.bookingService = bookingService;
		this.queryService = queryService;
		this.mapper = mapper;
	}

	@Operation(
		summary = "Book a new delivery",
		description = """
			Books a delivery for a user in the specified timeslot.
			
			**Business Rules:**
			- The timeslot must exist and be available
			- Daily capacity must not be exceeded
			- Timeslot capacity must not be exceeded
			- The timeslot must support the user's address (checked separately via /timeslots endpoint)
			
			**Returns:** The created delivery with status PENDING
			""",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Booking request with user and timeslot information",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = BookDeliveryRequest.class),
				examples = @ExampleObject(
					name = "Book delivery",
					value = """
						{
						  "user": "john.doe@example.com",
						  "timeslotId": "ts-2025-10-30-morning"
						}
						"""
				)
			)
		)
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "201",
			description = "Delivery successfully created",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = DeliveryDTO.class),
				examples = @ExampleObject(
					value = """
						{
						  "id": "d1f8e9a3-4b2c-4d5e-8f6a-7b8c9d0e1f2a",
						  "user": "john.doe@example.com",
						  "timeslotId": "ts-2025-10-30-morning",
						  "status": "PENDING"
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "Timeslot not found",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						  "timestamp": "2025-10-30T14:23:45.123Z",
						  "status": 404,
						  "message": "unknown timeslot",
						  "error": "UNKNOWN_TIMESLOT"
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "409",
			description = "Capacity limit reached",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						  "timestamp": "2025-10-30T14:23:45.123Z",
						  "status": 409,
						  "message": "timeslot capacity reached",
						  "error": "TIMESLOT_CAPACITY_REACHED"
						}
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
						    "user": "User is required",
						    "timeslotId": "Timeslot ID is required"
						  }
						}
						"""
				)
			)
		)
	})
	@PostMapping
	public ResponseEntity<DeliveryDTO> book(@Valid @RequestBody BookDeliveryRequest request) {
		// @Valid + @RequestBody ensures request is not null and validated
		Delivery delivery = bookingService.book(request.getUser(), request.getTimeslotId());
		
		if (delivery == null) {
			throw new RuntimeException("Booking failed");
		}

		DeliveryDTO dto = mapper.toDto(delivery);
		
		// Build Location header URI: /deliveries/{id}
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(dto.getId())
				.toUri();
		
		// Return 201 Created with Location header
		return ResponseEntity.created(location).body(dto);
	}

	@Operation(
		summary = "Complete a delivery",
		description = "Marks a delivery as completed. Idempotent - calling multiple times has no side effect."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Delivery marked as completed",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = DeliveryDTO.class),
				examples = @ExampleObject(
					value = """
						{
						  "id": "d1f8e9a3-4b2c-4d5e-8f6a-7b8c9d0e1f2a",
						  "user": "john.doe@example.com",
						  "timeslotId": "ts-2025-10-30-morning",
						  "status": "COMPLETED"
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "Delivery not found",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						  "timestamp": "2025-10-30T14:23:45.123Z",
						  "status": 404,
						  "message": "delivery not found",
						  "error": "DELIVERY_NOT_FOUND"
						}
						"""
				)
			)
		)
	})
	@PostMapping("/{id}/complete")
	public DeliveryDTO complete(
		@Parameter(description = "Delivery ID", example = "d1f8e9a3-4b2c-4d5e-8f6a-7b8c9d0e1f2a")
		@PathVariable("id") String id
	) {
		Delivery delivery = bookingService.complete(id);
		
		if (delivery == null) {
			throw new RuntimeException("Delivery completion failed");
		}

		return mapper.toDto(delivery);
	}

	@Operation(
		summary = "Cancel a delivery",
		description = """
			Cancels a delivery and releases the capacity.
			
			**Important:** This releases both the timeslot and daily capacity, allowing other users to book.
			Idempotent - calling multiple times has no side effect.
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "204",
			description = "Delivery successfully cancelled (no content returned)"
		),
		@ApiResponse(
			responseCode = "404",
			description = "Delivery not found",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						  "timestamp": "2025-10-30T14:23:45.123Z",
						  "status": 404,
						  "message": "delivery not found",
						  "error": "DELIVERY_NOT_FOUND"
						}
						"""
				)
			)
		)
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> cancel(
		@Parameter(description = "Delivery ID", example = "d1f8e9a3-4b2c-4d5e-8f6a-7b8c9d0e1f2a")
		@PathVariable("id") String id
	) {
		// @PathVariable ensures id is extracted from URL (Spring handles null/empty)
		bookingService.cancel(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(
		summary = "Get today's deliveries",
		description = "Retrieves all deliveries scheduled for today (based on server time)."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "List of today's deliveries (may be empty)",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = DeliveryDTO.class),
				examples = @ExampleObject(
					value = """
						[
						  {
						    "id": "d1f8e9a3-4b2c-4d5e-8f6a-7b8c9d0e1f2a",
						    "user": "john.doe@example.com",
						    "timeslotId": "ts-2025-10-30-morning",
						    "status": "PENDING"
						  },
						  {
						    "id": "e2g9f0b4-5c3d-5e6f-9g7b-8c9d0e1f2b3a",
						    "user": "jane.smith@example.com",
						    "timeslotId": "ts-2025-10-30-afternoon",
						    "status": "COMPLETED"
						  }
						]
						"""
				)
			)
		)
	})
	@GetMapping("/daily")
	public List<DeliveryDTO> today() { 
		return queryService.today(); 
	}

	@Operation(
		summary = "Get this week's deliveries",
		description = "Retrieves all deliveries scheduled for the current week (Monday to Sunday)."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "List of this week's deliveries (may be empty)",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = DeliveryDTO.class),
				examples = @ExampleObject(
					value = """
						[
						  {
						    "id": "d1f8e9a3-4b2c-4d5e-8f6a-7b8c9d0e1f2a",
						    "user": "john.doe@example.com",
						    "timeslotId": "ts-2025-10-28-morning",
						    "status": "COMPLETED"
						  },
						  {
						    "id": "e2g9f0b4-5c3d-5e6f-9g7b-8c9d0e1f2b3a",
						    "user": "jane.smith@example.com",
						    "timeslotId": "ts-2025-10-30-afternoon",
						    "status": "PENDING"
						  }
						]
						"""
				)
			)
		)
	})
	@GetMapping("/weekly")
	public List<DeliveryDTO> weekly() { 
		return queryService.weekly(); 
	}
}
