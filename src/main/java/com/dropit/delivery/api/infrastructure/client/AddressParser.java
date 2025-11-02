package com.dropit.delivery.api.infrastructure.client;

import com.dropit.delivery.api.domain.model.Address;
import org.springframework.stereotype.Component;

/**
 * Parses free-text addresses into structured Address objects.
 * Handles naive parsing logic without external dependencies.
 */
@Component
public class AddressParser {

	public Address parse(String searchTerm) {
		String[] parts = searchTerm.split(",");
		if (parts.length == 0) {
			return emptyAddress();
		}

		var parsedAddress = new ParsedAddress();
		
		// Parse street and house number from first part
		parseStreetAndNumber(parts[0].trim(), parsedAddress);
		
		// Parse remaining parts: city, country, postcode
		if (parts.length >= 2) {
			parsedAddress.city = parts[1].trim();
		}
		
		// Process remaining parts (country/postcode can appear in different orders)
		for (int i = 2; i < parts.length; i++) {
			String part = parts[i].trim();
			if (isPostcode(part)) {
				parsedAddress.postcode = part;
			} else if (parsedAddress.country.isEmpty()) {
				parsedAddress.country = part;
			}
		}
		
		return new Address(
				parsedAddress.street,
				parsedAddress.line1,
				"",
				parsedAddress.country,
				parsedAddress.postcode,
				parsedAddress.city
		);
	}

	private void parseStreetAndNumber(String addressPart, ParsedAddress parsedAddress) {
		if (addressPart.isEmpty()) {
			return;
		}

		String[] tokens = addressPart.split("\\s+");
		if (tokens.length == 0) {
			parsedAddress.street = addressPart;
			return;
		}

		// Check if last token looks like a house number (starts with digit)
		String lastToken = tokens[tokens.length - 1];
		if (lastToken.matches("\\d+.*")) {
			parsedAddress.line1 = lastToken;
			parsedAddress.street = addressPart.substring(0, addressPart.lastIndexOf(lastToken)).trim();
		} else {
			parsedAddress.street = addressPart;
		}
	}

	private boolean isPostcode(String value) {
		return !value.isEmpty() && value.matches("\\d+");
	}

	private Address emptyAddress() {
		return new Address("", "", "", "", "", "");
	}

	// Helper class for building address during parsing
	private static class ParsedAddress {
		String street = "";
		String line1 = "";
		String city = "";
		String postcode = "";
		String country = "";
	}
}

