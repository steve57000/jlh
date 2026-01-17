package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.AdminDashboardStatsDto;
import com.jlh.jlhautopambackend.dto.AdminYearlyStatsDto;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.DevisRepository;
import com.jlh.jlhautopambackend.repository.RendezVousRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardStatsServiceImpl implements AdminDashboardStatsService {

    private final DemandeServiceRepository demandeServiceRepository;
    private final DevisRepository devisRepository;
    private final RendezVousRepository rendezVousRepository;

    public AdminDashboardStatsServiceImpl(
            DemandeServiceRepository demandeServiceRepository,
            DevisRepository devisRepository,
            RendezVousRepository rendezVousRepository
    ) {
        this.demandeServiceRepository = demandeServiceRepository;
        this.devisRepository = devisRepository;
        this.rendezVousRepository = rendezVousRepository;
    }

    @Override
    public AdminDashboardStatsDto getStats() {
        int currentYear = Year.now().getValue();
        Map<Integer, AdminYearlyStatsDto> statsByYear = new HashMap<>();

        for (int year = currentYear; year >= currentYear - 4; year--) {
            statsByYear.put(year, AdminYearlyStatsDto.builder()
                    .year(year)
                    .serviceCount(0)
                    .serviceRevenue(BigDecimal.ZERO)
                    .devisCount(0)
                    .devisRevenue(BigDecimal.ZERO)
                    .rendezVousCount(0)
                    .forecast(false)
                    .build());
        }

        demandeServiceRepository.aggregateYearlyServiceStats().forEach(row -> {
            AdminYearlyStatsDto entry = statsByYear.get(row.getYear());
            if (entry != null) {
                entry.setServiceCount(row.getCount() != null ? row.getCount() : 0);
                entry.setServiceRevenue(row.getAmount() != null ? row.getAmount() : BigDecimal.ZERO);
            }
        });

        devisRepository.aggregateYearlyDevisStats().forEach(row -> {
            AdminYearlyStatsDto entry = statsByYear.get(row.getYear());
            if (entry != null) {
                entry.setDevisCount(row.getCount() != null ? row.getCount() : 0);
                entry.setDevisRevenue(row.getAmount() != null ? row.getAmount() : BigDecimal.ZERO);
            }
        });

        rendezVousRepository.aggregateYearlyRendezVousStats().forEach(row -> {
            AdminYearlyStatsDto entry = statsByYear.get(row.getYear());
            if (entry != null) {
                entry.setRendezVousCount(row.getCount() != null ? row.getCount() : 0);
            }
        });

        List<AdminYearlyStatsDto> yearly = new ArrayList<>(statsByYear.values());
        yearly.sort(Comparator.comparing(AdminYearlyStatsDto::getYear).reversed());

        AdminYearlyStatsDto forecast = buildForecast(currentYear + 1, yearly);
        yearly.add(forecast);

        return AdminDashboardStatsDto.builder()
                .currentYear(currentYear)
                .yearly(yearly)
                .build();
    }

    private AdminYearlyStatsDto buildForecast(int forecastYear, List<AdminYearlyStatsDto> baseYears) {
        List<AdminYearlyStatsDto> recent = baseYears.stream()
                .filter(entry -> entry.getYear() >= forecastYear - 3 && entry.getYear() < forecastYear)
                .toList();

        long divisor = Math.max(1, recent.size());
        long serviceCount = Math.round(recent.stream().mapToLong(AdminYearlyStatsDto::getServiceCount).average().orElse(0));
        long devisCount = Math.round(recent.stream().mapToLong(AdminYearlyStatsDto::getDevisCount).average().orElse(0));
        long rdvCount = Math.round(recent.stream().mapToLong(AdminYearlyStatsDto::getRendezVousCount).average().orElse(0));

        BigDecimal serviceRevenue = averageAmount(recent.stream()
                .map(AdminYearlyStatsDto::getServiceRevenue)
                .toList(), divisor);
        BigDecimal devisRevenue = averageAmount(recent.stream()
                .map(AdminYearlyStatsDto::getDevisRevenue)
                .toList(), divisor);

        return AdminYearlyStatsDto.builder()
                .year(forecastYear)
                .serviceCount(serviceCount)
                .serviceRevenue(serviceRevenue)
                .devisCount(devisCount)
                .devisRevenue(devisRevenue)
                .rendezVousCount(rdvCount)
                .forecast(true)
                .build();
    }

    private BigDecimal averageAmount(List<BigDecimal> values, long divisor) {
        BigDecimal total = values.stream()
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (divisor <= 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }
}
