package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.GarageOpeningHour;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourExceptionalType;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourScope;
import com.jlh.jlhautopambackend.repository.GarageOpeningHourRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GarageOpeningHourCleanupService {

    private static final Logger log = LoggerFactory.getLogger(GarageOpeningHourCleanupService.class);

    private final GarageOpeningHourRepository repository;
    private final ZoneId zoneId;

    public GarageOpeningHourCleanupService(GarageOpeningHourRepository repository,
                                           @Value("${garage.timezone:Europe/Paris}") String timezone) {
        this.repository = repository;
        this.zoneId = ZoneId.of(timezone);
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "${garage.timezone:Europe/Paris}")
    @Transactional
    public void removeExpiredExceptionalHours() {
        LocalDate today = ZonedDateTime.now(zoneId).toLocalDate();
        List<GarageOpeningHour> expired = new ArrayList<>();
        expired.addAll(repository.findByScopeAndExceptionalTypeAndExceptionalDateLessThan(
                GarageOpeningHourScope.EXCEPTIONAL,
                GarageOpeningHourExceptionalType.SINGLE_DAY,
                today));
        expired.addAll(repository.findByScopeAndExceptionalTypeAndExceptionalEndDateLessThan(
                GarageOpeningHourScope.EXCEPTIONAL,
                GarageOpeningHourExceptionalType.PERIOD,
                today));

        if (expired.isEmpty()) {
            return;
        }

        repository.deleteAllInBatch(expired);
        log.info("{} horaires exceptionnels expirés supprimés (date < {}).", expired.size(), today);
    }
}
