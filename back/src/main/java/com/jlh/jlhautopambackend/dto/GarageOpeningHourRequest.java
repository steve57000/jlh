package com.jlh.jlhautopambackend.dto;

import com.jlh.jlhautopambackend.modeles.GarageOpeningHourExceptionalType;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourScope;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourStatus;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageOpeningHourRequest {
    private GarageOpeningHourScope scope;
    private GarageOpeningHourStatus status;
    private GarageOpeningHourType openingType;
    private DayOfWeek dayOfWeek;
    private GarageOpeningHourExceptionalType exceptionalType;
    private LocalDate exceptionalDate;
    private LocalDate exceptionalStartDate;
    private LocalDate exceptionalEndDate;
    private String label;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime startTime2;
    private LocalTime endTime2;
}
