package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.GarageOpeningHourRequest;
import com.jlh.jlhautopambackend.dto.GarageOpeningHourResponse;
import java.util.List;
import java.util.Optional;

public interface GarageOpeningHourService {
    List<GarageOpeningHourResponse> findAll();
    Optional<GarageOpeningHourResponse> findById(Integer id);
    GarageOpeningHourResponse create(GarageOpeningHourRequest request);
    Optional<GarageOpeningHourResponse> update(Integer id, GarageOpeningHourRequest request);
    boolean delete(Integer id);
}
