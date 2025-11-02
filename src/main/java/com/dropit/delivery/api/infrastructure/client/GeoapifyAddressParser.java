package com.dropit.delivery.api.infrastructure.client;

import com.dropit.delivery.api.domain.model.Address;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Parses Geoapify API JSON responses into Address objects.
 * Handles different field name variations from Geoapify API.
 */
@Component
public class GeoapifyAddressParser {
	private static final Logger logger = LoggerFactory.getLogger(GeoapifyAddressParser.class);

	public Address parse(JsonNode json) {
		var results = json.path("results");

		if (!results.isArray() || results.size() == 0) {
			logger.debug("No results in Geoapify API response");
			return null; // Indicate no results
		}

		var firstResult = results.get(0);
		
		String street = getFieldOrFallback(firstResult, "street", "address_line1");
		String housenumber = getFieldOrFallback(firstResult, "housenumber", "house_number");
		String country = getFieldOrFallback(firstResult, "country_code", "country");
		String postcode = getFieldValue(firstResult, "postcode");
		String city = getFieldValue(firstResult, "city");
		
		logger.debug("Parsed Geoapify response: street={}, housenumber={}, city={}, postcode={}, country={}", 
				street, housenumber, city, postcode, country);
		
		return new Address(street, housenumber, "", country, postcode, city);
	}

	private String getFieldOrFallback(JsonNode node, String primaryField, String fallbackField) {
		String value = getFieldValue(node, primaryField);
		return value.isEmpty() ? getFieldValue(node, fallbackField) : value;
	}

	private String getFieldValue(JsonNode node, String fieldName) {
		return node.path(fieldName).asText("");
	}
}

