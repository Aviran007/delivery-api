package com.dropit.delivery.api.application.service;

import com.dropit.delivery.api.api.dto.AddressDTO;
import com.dropit.delivery.api.domain.model.Timeslot;

import java.util.List;

public interface ITimeslotService {
	List<Timeslot> availableTimeslots(AddressDTO address);
}

