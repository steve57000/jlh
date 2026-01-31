package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.AdminDashboardAnalyticsDto;
import com.jlh.jlhautopambackend.dto.AdminDashboardRevenueStatDto;
import com.jlh.jlhautopambackend.dto.AdminDashboardServiceStatDto;
import com.jlh.jlhautopambackend.dto.AdminDashboardStatsDto;
import com.jlh.jlhautopambackend.dto.AdminDashboardTypeStatDto;
import com.jlh.jlhautopambackend.dto.AdminYearlyStatsDto;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.modeles.RendezVous;
import com.jlh.jlhautopambackend.modeles.ServicePrixMode;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.DevisRepository;
import com.jlh.jlhautopambackend.repository.RendezVousRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardStatsServiceImpl implements AdminDashboardStatsService {

    private final DemandeRepository demandeRepository;
    private final DemandeServiceRepository demandeServiceRepository;
    private final DevisRepository devisRepository;
    private final RendezVousRepository rendezVousRepository;

    public AdminDashboardStatsServiceImpl(
            DemandeRepository demandeRepository,
            DemandeServiceRepository demandeServiceRepository,
            DevisRepository devisRepository,
            RendezVousRepository rendezVousRepository
    ) {
        this.demandeRepository = demandeRepository;
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

    @Override
    public AdminDashboardAnalyticsDto getAnalytics(AdminDashboardAnalyticsFilter filter) {
        List<Demande> demandes = demandeRepository.findAll();
        List<Demande> filtered = filterDemandes(demandes, filter);
        long totalDemandes = filtered.size();
        long pending = filtered.stream().filter(d -> codeEquals(d, "En_attente")).count();
        long traitees = filtered.stream().filter(d -> codeEquals(d, "Traitee")).count();
        long annulees = filtered.stream().filter(d -> codeEquals(d, "Annulee")).count();

        long devisTotal = filtered.stream().filter(d -> typeEquals(d, "Devis")).count();
        long devisAvecRdv = filtered.stream()
                .filter(d -> typeEquals(d, "Devis"))
                .filter(d -> {
                    RendezVous rdv = d.getRendezVous();
                    Devis devis = d.getDevis();
                    return rdv != null || (devis != null && devis.getRendezVousId() != null);
                })
                .count();

        BigDecimal revenueTotal = filtered.stream()
                .map(this::computeDemandeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminDashboardTypeStatDto> typeStats = buildTypeStats(filtered, totalDemandes);
        List<AdminDashboardServiceStatDto> serviceStats = buildServiceStats(filtered);
        List<AdminDashboardRevenueStatDto> revenueStats = buildRevenueStats(filtered);
        List<AdminYearlyStatsDto> yearly = buildYearlyStats(filtered, filter.includeForecast());

        return AdminDashboardAnalyticsDto.builder()
                .totalDemandes(totalDemandes)
                .pending(pending)
                .traitees(traitees)
                .annulees(annulees)
                .devisTotal(devisTotal)
                .devisAvecRdv(devisAvecRdv)
                .devisSansSuite(Math.max(0, devisTotal - devisAvecRdv))
                .revenueTotal(revenueTotal)
                .typeStats(typeStats)
                .serviceStats(serviceStats)
                .revenueStats(revenueStats)
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

    private List<Demande> filterDemandes(List<Demande> demandes, AdminDashboardAnalyticsFilter filter) {
        Instant from = filter.from();
        Instant to = filter.to();
        Set<String> types = filter.types() != null ? Set.copyOf(filter.types()) : Set.of();
        Set<String> statuts = filter.statuts() != null ? Set.copyOf(filter.statuts()) : Set.of();
        Set<Integer> serviceIds = filter.serviceIds() != null ? Set.copyOf(filter.serviceIds()) : Set.of();

        return demandes.stream()
                .filter(d -> {
                    Instant date = d.getDateDemande();
                    if (from != null && (date == null || date.isBefore(from))) {
                        return false;
                    }
                    if (to != null && (date == null || date.isAfter(to))) {
                        return false;
                    }
                    if (!types.isEmpty() && (d.getTypeDemande() == null
                            || !types.contains(d.getTypeDemande().getCodeType()))) {
                        return false;
                    }
                    if (!statuts.isEmpty() && (d.getStatutDemande() == null
                            || !statuts.contains(d.getStatutDemande().getCodeStatut()))) {
                        return false;
                    }
                    if (!serviceIds.isEmpty()) {
                        return d.getServices() != null && d.getServices().stream()
                                .map(ds -> ds.getService() != null ? ds.getService().getIdService() : null)
                                .filter(Objects::nonNull)
                                .anyMatch(serviceIds::contains);
                    }
                    return true;
                })
                .toList();
    }

    private boolean typeEquals(Demande demande, String code) {
        return demande.getTypeDemande() != null && code.equals(demande.getTypeDemande().getCodeType());
    }

    private boolean codeEquals(Demande demande, String code) {
        return demande.getStatutDemande() != null && code.equals(demande.getStatutDemande().getCodeStatut());
    }

    private BigDecimal computeDemandeAmount(Demande demande) {
        if (demande == null || demande.getServices() == null) {
            return BigDecimal.ZERO;
        }
        return demande.getServices().stream()
                .map(this::computeServiceLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal computeServiceLineAmount(DemandeService service) {
        if (service == null || service.getPrixUnitaireService() == null) {
            return BigDecimal.ZERO;
        }
        int qty = service.getQuantite() != null ? service.getQuantite() : 1;
        ServicePrixMode mode = service.getPrixModeService() != null
                ? service.getPrixModeService()
                : ServicePrixMode.UNITAIRE;
        if (mode == ServicePrixMode.LOT) {
            int lotSize = service.getTailleLotService() != null ? service.getTailleLotService() : 1;
            if (lotSize > 0) {
                int lots = qty / lotSize;
                return service.getPrixUnitaireService().multiply(BigDecimal.valueOf(lots));
            }
        }
        return service.getPrixUnitaireService().multiply(BigDecimal.valueOf(qty));
    }

    private List<AdminDashboardTypeStatDto> buildTypeStats(List<Demande> demandes, long total) {
        Map<String, Long> counts = demandes.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getTypeDemande() != null ? d.getTypeDemande().getCodeType() : "Inconnu",
                        Collectors.counting()
                ));
        Map<String, BigDecimal> totals = demandes.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getTypeDemande() != null ? d.getTypeDemande().getCodeType() : "Inconnu",
                        Collectors.mapping(this::computeDemandeAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String type = entry.getKey();
                    long count = entry.getValue();
                    BigDecimal amount = totals.getOrDefault(type, BigDecimal.ZERO);
                    int percentage = total > 0 ? Math.toIntExact(Math.round((double) count * 100 / total)) : 0;
                    double average = count > 0 ? amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP).doubleValue() : 0;
                    return AdminDashboardTypeStatDto.builder()
                            .type(type)
                            .label(type)
                            .count(count)
                            .percentage(percentage)
                            .averageValue(average)
                            .build();
                })
                .toList();
    }

    private List<AdminDashboardServiceStatDto> buildServiceStats(List<Demande> demandes) {
        Map<String, Long> counts = new HashMap<>();
        Map<String, BigDecimal> revenue = new HashMap<>();

        for (Demande demande : demandes) {
            if (demande.getServices() == null) {
                continue;
            }
            for (DemandeService service : demande.getServices()) {
                String label = service.getService() != null && service.getService().getLibelle() != null
                        ? service.getService().getLibelle()
                        : "Service";
                counts.put(label, counts.getOrDefault(label, 0L) + 1);
                revenue.put(label, revenue.getOrDefault(label, BigDecimal.ZERO).add(computeServiceLineAmount(service)));
            }
        }

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(entry -> AdminDashboardServiceStatDto.builder()
                        .label(entry.getKey())
                        .count(entry.getValue())
                        .percentage(total > 0 ? Math.toIntExact(Math.round((double) entry.getValue() * 100 / total)) : 0)
                        .revenue(revenue.getOrDefault(entry.getKey(), BigDecimal.ZERO))
                        .build())
                .toList();
    }

    private List<AdminDashboardRevenueStatDto> buildRevenueStats(List<Demande> demandes) {
        Map<String, BigDecimal> revenue = new HashMap<>();
        for (Demande demande : demandes) {
            if (demande.getServices() == null) {
                continue;
            }
            for (DemandeService service : demande.getServices()) {
                String label = service.getService() != null && service.getService().getLibelle() != null
                        ? service.getService().getLibelle()
                        : "Service";
                revenue.put(label, revenue.getOrDefault(label, BigDecimal.ZERO).add(computeServiceLineAmount(service)));
            }
        }
        BigDecimal total = revenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return revenue.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> {
                    int percentage = total.compareTo(BigDecimal.ZERO) > 0
                            ? entry.getValue().multiply(BigDecimal.valueOf(100))
                            .divide(total, 0, RoundingMode.HALF_UP)
                            .intValue()
                            : 0;
                    return AdminDashboardRevenueStatDto.builder()
                            .label(entry.getKey())
                            .amount(entry.getValue())
                            .percentage(percentage)
                            .build();
                })
                .toList();
    }

    private List<AdminYearlyStatsDto> buildYearlyStats(List<Demande> demandes, boolean includeForecast) {
        Map<Integer, AdminYearlyStatsDto> statsByYear = new TreeMap<>();

        for (Demande demande : demandes) {
            if (demande.getDateDemande() == null) {
                continue;
            }
            int year = demande.getDateDemande().atZone(java.time.ZoneOffset.UTC).getYear();
            AdminYearlyStatsDto entry = statsByYear.computeIfAbsent(year, y -> AdminYearlyStatsDto.builder()
                    .year(y)
                    .serviceCount(0)
                    .serviceRevenue(BigDecimal.ZERO)
                    .devisCount(0)
                    .devisRevenue(BigDecimal.ZERO)
                    .rendezVousCount(0)
                    .forecast(false)
                    .build());

            if (demande.getServices() != null && !demande.getServices().isEmpty()) {
                entry.setServiceCount(entry.getServiceCount() + demande.getServices().size());
                BigDecimal sum = demande.getServices().stream()
                        .map(this::computeServiceLineAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                entry.setServiceRevenue(entry.getServiceRevenue().add(sum));
            }
            if (demande.getDevis() != null) {
                entry.setDevisCount(entry.getDevisCount() + 1);
                BigDecimal montant = demande.getDevis().getMontantTotal() != null
                        ? demande.getDevis().getMontantTotal()
                        : BigDecimal.ZERO;
                entry.setDevisRevenue(entry.getDevisRevenue().add(montant));
            }
            if (demande.getRendezVous() != null) {
                entry.setRendezVousCount(entry.getRendezVousCount() + 1);
            }
        }

        List<AdminYearlyStatsDto> yearly = new ArrayList<>(statsByYear.values());
        yearly.sort(Comparator.comparing(AdminYearlyStatsDto::getYear).reversed());
        if (includeForecast && !yearly.isEmpty()) {
            int nextYear = yearly.get(0).getYear() + 1;
            yearly.add(buildForecast(nextYear, yearly));
        }
        return yearly;
    }
}
