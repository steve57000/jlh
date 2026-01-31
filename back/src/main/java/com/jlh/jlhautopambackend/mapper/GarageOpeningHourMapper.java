package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.GarageOpeningHourRequest;
import com.jlh.jlhautopambackend.dto.GarageOpeningHourResponse;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHour;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class GarageOpeningHourMapper {

    public GarageOpeningHourResponse toResponse(GarageOpeningHour entity) {
        return GarageOpeningHourResponse.builder()
                .idOpeningHour(entity.getIdOpeningHour())
                .scope(entity.getScope())
                .status(entity.getStatus())
                .openingType(entity.getOpeningType())
                .dayOfWeek(entity.getDayOfWeek())
                .exceptionalType(entity.getExceptionalType())
                .exceptionalDate(entity.getExceptionalDate())
                .exceptionalStartDate(entity.getExceptionalStartDate())
                .exceptionalEndDate(entity.getExceptionalEndDate())
                .label(entity.getLabel())
                .description(entity.getDescription())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .startTime2(entity.getStartTime2())
                .endTime2(entity.getEndTime2())
                .build();
    }

    public GarageOpeningHour toEntity(GarageOpeningHourRequest request) {
        return GarageOpeningHour.builder()
                .scope(request.getScope())
                .status(request.getStatus())
                .openingType(request.getOpeningType())
                .dayOfWeek(request.getDayOfWeek())
                .exceptionalType(request.getExceptionalType())
                .exceptionalDate(request.getExceptionalDate())
                .exceptionalStartDate(request.getExceptionalStartDate())
                .exceptionalEndDate(request.getExceptionalEndDate())
                .label(request.getLabel())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .startTime2(request.getStartTime2())
                .endTime2(request.getEndTime2())
                .build();
    }
}
