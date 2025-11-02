package com.dropit.delivery.api.application.service;

import com.dropit.delivery.api.api.dto.DeliveryDTO;
import com.dropit.delivery.api.api.mapper.DtoMapper;
import com.dropit.delivery.api.domain.repository.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueryService implements IQueryService {
	private static final Logger logger = LoggerFactory.getLogger(QueryService.class);
	private final DeliveryRepository deliveryRepository;
	private final DtoMapper mapper;

	public QueryService(DeliveryRepository deliveryRepository, DtoMapper mapper) {
		this.deliveryRepository = deliveryRepository;
		this.mapper = mapper;
	}

	@Override
	public List<DeliveryDTO> today() {
		try {
			return findDeliveriesByDate(LocalDate.now());
		} catch (Exception e) {
			logger.error("Failed to retrieve today's deliveries", e);
			return List.of();
		}
	}

	@Override
	public List<DeliveryDTO> weekly() {
		try {
			return findDeliveriesByWeek(LocalDate.now());
		} catch (Exception e) {
			logger.error("Failed to retrieve weekly deliveries", e);
			return List.of();
		}
	}

	private List<DeliveryDTO> findDeliveriesByDate(LocalDate date) {
		return deliveryRepository.findByDate(date).stream()
				.map(mapper::toDto)
				.collect(Collectors.toList());
	}

	private List<DeliveryDTO> findDeliveriesByWeek(LocalDate date) {
		return deliveryRepository.findByWeek(date).stream()
				.map(mapper::toDto)
				.collect(Collectors.toList());
	}
}
