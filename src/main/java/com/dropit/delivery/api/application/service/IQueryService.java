package com.dropit.delivery.api.application.service;

import com.dropit.delivery.api.api.dto.DeliveryDTO;

import java.util.List;

public interface IQueryService {
	List<DeliveryDTO> today();
	List<DeliveryDTO> weekly();
}

