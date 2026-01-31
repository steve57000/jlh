package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "garage_opening_hour")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageOpeningHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opening_hour")
    private Integer idOpeningHour;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private GarageOpeningHourScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GarageOpeningHourStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "opening_type")
    private GarageOpeningHourType openingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "exceptional_type")
    private GarageOpeningHourExceptionalType exceptionalType;

    @Column(name = "exceptional_date")
    private LocalDate exceptionalDate;

    @Column(name = "exceptional_start_date")
    private LocalDate exceptionalStartDate;

    @Column(name = "exceptional_end_date")
    private LocalDate exceptionalEndDate;

    @Column(name = "label")
    private String label;

    @Column(name = "description")
    private String description;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "start_time_2")
    private LocalTime startTime2;

    @Column(name = "end_time_2")
    private LocalTime endTime2;
}
