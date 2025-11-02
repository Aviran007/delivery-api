package com.dropit.delivery.api.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class HolidayClient extends AbstractHttpClient {
	private static final Logger logger = LoggerFactory.getLogger(HolidayClient.class);
    private static final Duration SUCCESS_TTL = Duration.ofHours(24);
    private static final Duration FAILURE_TTL = Duration.ofHours(1);

    private static final class CacheEntry {
        final Set<LocalDate> payload;
        final Instant expiresAt;

        CacheEntry(Set<LocalDate> payload, Instant expiresAt) {
            this.payload = payload;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

	public HolidayClient(@Value("${holiday.api.base-url}") String baseUrl,
						 @Value("${holiday.api.key}") String apiKey) {
		super(baseUrl, apiKey);
	}

	public Set<LocalDate> holidaysForYear(String country, int year) {
		if (!isApiConfigured()) {
			return Set.of();
		}

		if (!StringUtils.hasText(country)) {
			return Set.of();
		}

		try {
            String key = cacheKey(country, year);
            CacheEntry cached = cache.get(key);
            if (cached != null && !cached.isExpired()) {
                logger.debug("Returning holidays from cache for key={}", key);
                return cached.payload;
            }

            Set<LocalDate> result = fetchHolidaysFromApi(country, year);
            cache.put(key, new CacheEntry(result, Instant.now().plus(SUCCESS_TTL)));
            return result;
		} catch (Exception e) {
			logger.warn("Failed to fetch holidays for country: {} and year: {}", country, year, e);
            String key = cacheKey(country, year);
            cache.put(key, new CacheEntry(Set.of(), Instant.now().plus(FAILURE_TTL)));
            return Set.of();
		}
	}

	private Set<LocalDate> fetchHolidaysFromApi(String country, int year) throws Exception {
		String url = buildHolidayApiUrl(country, year);
		logger.debug("Fetching public holidays from API: {}", url.replace(apiKey, "***"));
		var json = fetchAndParseJson(url);
		return parseHolidaysResponse(json);
	}

	private String buildHolidayApiUrl(String country, int year) {
		// URL encode parameters to handle special characters safely
		String encodedCountry = URLEncoder.encode(country, StandardCharsets.UTF_8);
		String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
		// Only fetch public holidays - private/religious holidays shouldn't block deliveries
		return baseUrl + "?pretty&country=" + encodedCountry + "&year=" + year + "&public=true&key=" + encodedApiKey;
	}

	private Set<LocalDate> parseHolidaysResponse(JsonNode json) {
		var holidaysArray = json.path("holidays");

		if (!holidaysArray.isArray()) {
			logger.debug("Holidays array is missing or not an array in API response");
			return Set.of();
		}

		Set<LocalDate> holidays = StreamSupport.stream(holidaysArray.spliterator(), false)
				.filter(this::isPublicHoliday) // Only include public holidays
				.map(this::parseHolidayDate)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());

		logger.debug("Parsed {} public holidays from API response", holidays.size());
		return holidays;
	}

	private boolean isPublicHoliday(JsonNode holiday) {
		// Double-check that holiday is public (in case API doesn't filter properly)
		boolean isPublic = holiday.path("public").asBoolean(false);
		if (!isPublic) {
			logger.debug("Skipping non-public holiday: {}", holiday.path("name").asText("unknown"));
		}
		return isPublic;
	}

	private Optional<LocalDate> parseHolidayDate(JsonNode holiday) {
		try {
			String dateStr = holiday.path("date").asText();
			if (StringUtils.hasText(dateStr)) {
				return Optional.of(LocalDate.parse(dateStr));
			}
		} catch (Exception e) {
			logger.debug("Failed to parse holiday date", e);
		}
		return Optional.empty();
	}

    private String cacheKey(String country, int year) {
        return (country + "|" + year).toUpperCase();
    }
}
