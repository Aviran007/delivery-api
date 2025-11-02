package com.dropit.delivery.api.api.mapper;

import com.dropit.delivery.api.api.dto.AddressDTO;
import com.dropit.delivery.api.api.dto.DeliveryDTO;
import com.dropit.delivery.api.domain.model.Address;
import com.dropit.delivery.api.domain.model.Delivery;
import com.dropit.delivery.api.domain.model.Timeslot;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DtoMapper {

	public AddressDTO toDto(Address address) {
		if (address == null) {
			return null;
		}

		AddressDTO dto = new AddressDTO();
		dto.setStreet(address.getStreet());
		dto.setLine1(address.getLine1());
		dto.setLine2(address.getLine2());
		dto.setCountry(address.getCountry());
		dto.setPostcode(address.getPostcode());
		dto.setCity(address.getCity());
		return dto;
	}

	public DeliveryDTO toDto(Delivery delivery) {
		if (delivery == null) {
			return null;
		}

		DeliveryDTO dto = new DeliveryDTO();
		dto.setId(delivery.getId());
		dto.setTimeslotId(delivery.getTimeslotId());
		dto.setUser(delivery.getUser());
		dto.setStatus(delivery.getStatus());
		return dto;
	}

	public Map<String, String> toTimeslotMap(Timeslot timeslot) {
		if (timeslot == null) {
			return Map.of();
		}

		return Map.of(
				"id", timeslot.getId(),
				"startTime", timeslot.getStartTime().toString(),
				"endTime", timeslot.getEndTime().toString());
	}
}

