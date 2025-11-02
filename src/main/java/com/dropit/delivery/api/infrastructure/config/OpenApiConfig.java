package com.dropit.delivery.api.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
	@Bean
	public OpenAPI deliveryOpenAPI() {
		Server localServer = new Server()
				.url("http://localhost:8080")
				.description("Local Development Server");

		Info info = new Info()
				.title("Delivery Scheduling API")
				.version("1.0.0")
				.description("""
						## Overview
						The Delivery Scheduling API allows you to manage delivery bookings, timeslots, and addresses.

						## Features
						- 📦 Book deliveries in available timeslots
						- 📅 View available timeslots for specific addresses
						- 🏠 Resolve free-text addresses to structured format
						- 📊 Query deliveries (daily/weekly)
						- ✅ Complete or cancel deliveries

						## Business Rules
						- Daily capacity limit per day
						- Timeslot capacity limit per slot
						- Holiday exclusions (configurable per country)
						- Address validation (country, postcode, city support)

						## Error Handling
						All endpoints return standardized error responses with:
						- HTTP status code
						- Timestamp
						- Error message
						- Error code (for business logic errors)
						""");

		return new OpenAPI()
				.info(info)
				.servers(List.of(localServer));
	}
}
