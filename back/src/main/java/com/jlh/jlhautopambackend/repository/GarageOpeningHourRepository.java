package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.GarageOpeningHour;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourExceptionalType;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourScope;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GarageOpeningHourRepository extends JpaRepository<GarageOpeningHour, Integer> {
    List<GarageOpeningHour> findByScopeAndExceptionalTypeAndExceptionalDateLessThan(
            GarageOpeningHourScope scope,
            GarageOpeningHourExceptionalType exceptionalType,
            LocalDate exceptionalDate);

    List<GarageOpeningHour> findByScopeAndExceptionalTypeAndExceptionalEndDateLessThan(
            GarageOpeningHourScope scope,
            GarageOpeningHourExceptionalType exceptionalType,
            LocalDate exceptionalEndDate);
}
