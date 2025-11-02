package com.dropit.delivery.api.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Structured address information")
public class AddressDTO {
	
	@Schema(
		description = "Street name",
		example = "Rothschild Boulevard",
		required = true
	)
	@NotBlank(message = "Street is required")
	private String street;
	
	@Schema(
		description = "House number or building name",
		example = "12",
		required = true
	)
	@NotBlank(message = "Line1 is required")
	private String line1;
	
	@Schema(
		description = "Additional address line (apartment, floor, etc.)",
		example = "Apt 5B"
	)
	private String line2;
	
	@Schema(
		description = "Country code (ISO 3166-1 alpha-2)",
		example = "IL",
		required = true
	)
	@NotBlank(message = "Country is required")
	private String country;
	
	@Schema(
		description = "Postal code",
		example = "6688102",
		required = true
	)
	@NotBlank(message = "Postcode is required")
	private String postcode;
	
	@Schema(
		description = "City name",
		example = "Tel Aviv",
		required = true
	)
	@NotBlank(message = "City is required")
	private String city;

	public String getStreet() { return street; }
	public void setStreet(String street) { this.street = street; }
	public String getLine1() { return line1; }
	public void setLine1(String line1) { this.line1 = line1; }
	public String getLine2() { return line2; }
	public void setLine2(String line2) { this.line2 = line2; }
	public String getCountry() { return country; }
	public void setCountry(String country) { this.country = country; }
	public String getPostcode() { return postcode; }
	public void setPostcode(String postcode) { this.postcode = postcode; }
	public String getCity() { return city; }
	public void setCity(String city) { this.city = city; }
}
