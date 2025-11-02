package com.dropit.delivery.api.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to resolve a free-text address to structured format")
public class ResolveAddressRequest {
	
	@Schema(
		description = "Free-text address to resolve (can be partial or complete address)",
		example = "Rothschild Boulevard 12, Tel Aviv, Israel",
		required = true
	)
	@NotBlank(message = "Search term is required")
	private String searchTerm;

	public String getSearchTerm() { return searchTerm; }
	public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
}
