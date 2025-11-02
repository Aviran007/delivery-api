package com.dropit.delivery.api.infrastructure.client;

import com.dropit.delivery.api.domain.model.Address;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Orchestrates address resolution using external API (Geoapify) with fallback to naive parsing.
 * Delegates parsing logic to specialized parsers (SRP: orchestration only).
 */
@Component
public class AddressResolverClient extends AbstractHttpClient {
	private static final Logger logger = LoggerFactory.getLogger(AddressResolverClient.class);
	
	private final AddressParser addressParser;
	private final GeoapifyAddressParser geoapifyParser;

	public AddressResolverClient(
			@Value("${geoapify.api.base-url}") String baseUrl,
			@Value("${geoapify.api.key}") String apiKey,
			AddressParser addressParser,
			GeoapifyAddressParser geoapifyParser) {
		super(baseUrl, apiKey);
		this.addressParser = addressParser;
		this.geoapifyParser = geoapifyParser;
	}

	public Address resolve(String searchTerm) {
		if (!StringUtils.hasText(searchTerm)) {
			return addressParser.parse("");
		}

		// If API key is not configured, use naive parsing
		if (!StringUtils.hasText(apiKey)) {
			return addressParser.parse(searchTerm);
		}

		// Try API first, fallback to naive parsing on failure
		try {
			Address apiAddress = resolveWithApi(searchTerm);
			if (apiAddress != null) {
				logger.debug("Successfully resolved address via Geoapify API for term: {}", searchTerm);
				return apiAddress;
			} else {
				logger.debug("No results from Geoapify API for term: {}, falling back to naive parsing", searchTerm);
			}
		} catch (Exception e) {
			logger.warn("Failed to resolve address via API for term: {}, falling back to naive parsing", searchTerm, e);
		}
		
		return addressParser.parse(searchTerm);
	}

	private Address resolveWithApi(String searchTerm) throws Exception {
		String url = buildApiUrl(searchTerm);
		logger.debug("Calling Geoapify API with URL: {}", url.replace(apiKey, "***"));
		
		JsonNode json = fetchAndParseJson(url);
		logger.debug("Geoapify API raw response (first 500 chars): {}", 
				json.toString().length() > 500 ? json.toString().substring(0, 500) : json.toString());
		logger.debug("Geoapify API response has 'results': {}, has 'features': {}", 
				json.has("results"), json.has("features"));
		
		// Check if response has results array
		if (json.has("results")) {
			JsonNode results = json.path("results");
			logger.debug("Results array size: {}", results.isArray() ? results.size() : "not an array");
		}
		
		return geoapifyParser.parse(json);
	}

	private String buildApiUrl(String searchTerm) {
		// Geoapify API works better when commas are replaced with spaces
		// URLEncoder.encode converts commas to %2C which Geoapify doesn't handle well
		// So we'll replace commas with spaces before encoding
		String cleanedTerm = searchTerm.replace(",", " ").replaceAll("\\s+", " ").trim();
		String encodedTerm = URLEncoder.encode(cleanedTerm, StandardCharsets.UTF_8);
		return baseUrl + "?text=" + encodedTerm + "&format=json&apiKey=" + apiKey;
	}
}
