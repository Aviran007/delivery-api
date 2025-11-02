package com.dropit.delivery.api.api.web.controller;

import com.dropit.delivery.api.infrastructure.client.AddressResolverClient;
import com.dropit.delivery.api.api.dto.AddressDTO;
import com.dropit.delivery.api.api.dto.ResolveAddressRequest;
import com.dropit.delivery.api.api.mapper.DtoMapper;
import com.dropit.delivery.api.domain.model.Address;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(
	name = "Addresses",
	description = "Endpoints for address resolution and geocoding"
)
public class AddressController {
	private final AddressResolverClient addressResolverClient;
	private final DtoMapper mapper;

	public AddressController(AddressResolverClient addressResolverClient, DtoMapper mapper) {
		this.addressResolverClient = addressResolverClient;
		this.mapper = mapper;
	}

	@Operation(
		summary = "Resolve free-text address",
		description = """
			Converts a free-text address string into a structured address format.
			
			**How it works:**
			1. If Geoapify API is configured, uses external geocoding service
			2. If API fails or is not configured, falls back to naive string parsing
			
			**Use Case:** Convert user input like "Rothschild 12, Tel Aviv" into structured format
			needed for timeslot availability checking.
			""",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Free-text address to resolve",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ResolveAddressRequest.class),
				examples = {
					@ExampleObject(
						name = "Complete address",
						value = """
							{
							  "searchTerm": "Rothschild Boulevard 12, Tel Aviv, Israel"
							}
							"""
					),
					@ExampleObject(
						name = "Partial address",
						value = """
							{
							  "searchTerm": "Rothschild 12"
							}
							"""
					)
				}
			)
		)
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Address successfully resolved",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = AddressDTO.class),
				examples = @ExampleObject(
					value = """
						{
						  "street": "Rothschild Boulevard",
						  "line1": "12",
						  "line2": "",
						  "country": "IL",
						  "postcode": "6688102",
						  "city": "Tel Aviv"
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
						    "searchTerm": "Search term is required"
						  }
						}
						"""
				)
			)
		)
	})
	@PostMapping("/resolve-address")
	public AddressDTO resolve(@Valid @RequestBody ResolveAddressRequest request) {
		Address address = addressResolverClient.resolve(request.getSearchTerm());
		
		if (address == null) {
			throw new RuntimeException("Address resolution failed");
		}

		return mapper.toDto(address);
	}
}

