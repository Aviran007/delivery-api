package com.dropit.delivery.api.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public abstract class AbstractHttpClient {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractHttpClient.class);
	protected final RestClient restClient;
	protected final ObjectMapper objectMapper;
	protected final String baseUrl;
	protected final String apiKey;

	protected AbstractHttpClient(String baseUrl, String apiKey) {
		this.restClient = RestClient.create();
		this.objectMapper = new ObjectMapper();
		this.baseUrl = baseUrl;
		this.apiKey = apiKey;
	}

	protected boolean isApiConfigured() {
		return StringUtils.hasText(apiKey);
	}

	protected JsonNode fetchAndParseJson(String url) throws Exception {
		try {
			ResponseEntity<String> response = restClient.get()
					.uri(url)
					.retrieve()
					.onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
						String sanitizedUrl = url.replace(apiKey, "***");
						logger.warn("HTTP error from external API: {} - Status: {}", sanitizedUrl, res.getStatusCode());
						if (res.getStatusCode().is4xxClientError()) {
							logger.warn("Client error (4xx) - possibly invalid API key or malformed request");
						} else if (res.getStatusCode().is5xxServerError()) {
							logger.warn("Server error (5xx) - external API is down or having issues");
						}
					})
					.toEntity(String.class);

			if (response.getBody() == null) {
				throw new IllegalStateException("Empty response body from external API");
			}

			if (!response.getStatusCode().is2xxSuccessful()) {
				logger.error("Non-2xx status code from external API: {} - Status: {}", 
						url.replace(apiKey, "***"), response.getStatusCode());
				throw new IllegalStateException("Non-2xx status code: " + response.getStatusCode());
			}

			return objectMapper.readTree(response.getBody());
		} catch (RestClientResponseException e) {
			logger.error("HTTP error from external API: {} - Status: {} - Body: {}", 
					url.replace(apiKey, "***"), e.getStatusCode(), e.getResponseBodyAsString(), e);
			throw new Exception("HTTP error: " + e.getStatusCode() + " - " + e.getMessage(), e);
		} catch (RestClientException e) {
			logger.error("RestClient exception from external API: {} - Error: {}", 
					url.replace(apiKey, "***"), e.getMessage(), e);
			throw new Exception("RestClient error: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Unexpected exception calling external API: {} - Error: {}", 
					url.replace(apiKey, "***"), e.getMessage(), e);
			throw e;
		}
	}
}
