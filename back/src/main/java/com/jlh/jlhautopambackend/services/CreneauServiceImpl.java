package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.config.GarageProperties;
import com.jlh.jlhautopambackend.dto.CreneauCalendarEntryDto;
import com.jlh.jlhautopambackend.dto.CreneauRequest;
import com.jlh.jlhautopambackend.dto.CreneauResponse;
import com.jlh.jlhautopambackend.mapper.CreneauMapper;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHour;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourExceptionalType;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourScope;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourStatus;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourType;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import com.jlh.jlhautopambackend.repository.CreneauRepository;
import com.jlh.jlhautopambackend.repository.GarageOpeningHourRepository;
import com.jlh.jlhautopambackend.repository.StatutCreneauRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreneauServiceImpl implements CreneauService {

    private final CreneauRepository repository;
    private final CreneauMapper mapper;
    private final GarageOpeningHourRepository openingHourRepository;
    private final StatutCreneauRepository statutCreneauRepository;
    private final GarageProperties garageProperties;
    private static final String STATUT_RESERVE = "Reserve";
    private static final String STATUT_INDISPONIBLE = "Indisponible";
    private static final String STATUT_LIBRE = "Libre";

    public CreneauServiceImpl(CreneauRepository repository,
                              CreneauMapper mapper,
                              GarageOpeningHourRepository openingHourRepository,
                              StatutCreneauRepository statutCreneauRepository,
                              GarageProperties garageProperties) {
        this.repository = repository;
        this.mapper = mapper;
        this.openingHourRepository = openingHourRepository;
        this.statutCreneauRepository = statutCreneauRepository;
        this.garageProperties = garageProperties;
    }

    @Override
    public CreneauResponse create(CreneauRequest request) {
        Creneau entity = mapper.toEntity(request);
        Creneau saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreneauResponse> findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreneauResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CreneauResponse> update(Integer id, CreneauRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    // mettre à jour les champs nécessaires
                    existing.setDateDebut(request.getDateDebut());
                    existing.setDateFin(request.getDateFin());
                    existing.setStatut(mapper.toEntity(request).getStatut());
                    Creneau updated = repository.save(existing);
                    return mapper.toResponse(updated);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreneauCalendarEntryDto> buildCalendar(Instant start, Instant end, Integer slotMinutes) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Les paramètres start et end sont requis.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Le paramètre end doit être après start.");
        }
        int slotSize = slotMinutes != null && slotMinutes > 0 ? slotMinutes : 60;
        ZoneId zoneId = resolveZoneId();
        ZonedDateTime startZdt = start.atZone(zoneId);
        ZonedDateTime endZdt = end.atZone(zoneId);

        List<Creneau> existing = repository.findOverlapping(start, end);
        Map<SlotKey, List<Creneau>> creneauxBySlot = existing.stream()
                .collect(Collectors.groupingBy(
                        c -> new SlotKey(c.getDateDebut(), c.getDateFin())
                ));

        List<GarageOpeningHour> openingHours = openingHourRepository.findAll();
        List<CreneauCalendarEntryDto> result = new ArrayList<>();

        LocalDate current = startZdt.toLocalDate();
        LocalDate endDate = endZdt.toLocalDate();
        while (!current.isAfter(endDate)) {
            List<TimeRange> openings = resolveOpeningRanges(current, openingHours);
            boolean hasOpenings = !openings.isEmpty();
            ZonedDateTime dayStart = current.atStartOfDay(zoneId);
            ZonedDateTime dayEnd = dayStart.plusDays(1);
            for (ZonedDateTime slotStartZdt = dayStart;
                 slotStartZdt.isBefore(dayEnd);
                 slotStartZdt = slotStartZdt.plusMinutes(slotSize)) {
                ZonedDateTime slotEndZdt = slotStartZdt.plusMinutes(slotSize);
                if (slotEndZdt.isAfter(dayEnd)) {
                    break;
                }
                Instant slotStart = slotStartZdt.toInstant();
                Instant slotEnd = slotEndZdt.toInstant();
                if (slotEnd.isAfter(start) && slotStart.isBefore(end)) {
                    LocalTime cursor = slotStartZdt.toLocalTime();
                    LocalTime next = slotEndZdt.toLocalTime();
                    boolean withinOpening = hasOpenings && openings.stream()
                            .anyMatch(range -> !cursor.isBefore(range.start())
                                    && !next.isAfter(range.end()));
                    List<Creneau> matched = creneauxBySlot
                            .getOrDefault(new SlotKey(slotStart, slotEnd), List.of());
                    SlotAvailability availability = withinOpening
                            ? resolveSlotAvailability(matched)
                            : SlotAvailability.closed(resolveStatut(STATUT_INDISPONIBLE));
                    StatutCreneau statut = availability.statut();
                    result.add(CreneauCalendarEntryDto.builder()
                            .idCreneau(resolveSlotId(matched))
                            .dateDebut(slotStart)
                            .dateFin(slotEnd)
                            .codeStatut(statut.getCodeStatut())
                            .libelleStatut(statut.getLibelle())
                            .totalCount(availability.totalCount())
                            .availableCount(availability.availableCount())
                            .reservedCount(availability.reservedCount())
                            .unavailableCount(availability.unavailableCount())
                            .build());
                }
            }
            current = current.plusDays(1);
        }

        return result.stream()
                .sorted(Comparator.comparing(CreneauCalendarEntryDto::getDateDebut))
                .toList();
    }

    private SlotAvailability resolveSlotAvailability(List<Creneau> creneaux) {
        if (creneaux == null || creneaux.isEmpty()) {
            return SlotAvailability.open(resolveStatut(STATUT_LIBRE), 0, 0, 0, 0);
        }
        int total = creneaux.size();
        int reserved = (int) creneaux.stream()
                .filter(creneau -> isStatut(creneau, STATUT_RESERVE))
                .count();
        int unavailable = (int) creneaux.stream()
                .filter(creneau -> isStatut(creneau, STATUT_INDISPONIBLE))
                .count();
        int available = total - reserved - unavailable;
        if (available > 0) {
            return SlotAvailability.open(resolveStatut(STATUT_LIBRE), total, available, reserved, unavailable);
        }
        if (reserved > 0) {
            return SlotAvailability.open(resolveStatut(STATUT_RESERVE), total, available, reserved, unavailable);
        }
        return SlotAvailability.open(resolveStatut(STATUT_INDISPONIBLE), total, available, reserved, unavailable);
    }

    private boolean isStatut(Creneau creneau, String code) {
        return creneau != null
                && creneau.getStatut() != null
                && code.equals(creneau.getStatut().getCodeStatut());
    }

    private StatutCreneau resolveStatut(String code) {
        return statutCreneauRepository.findById(code)
                .orElseThrow(() -> new IllegalStateException("StatutCreneau introuvable: " + code));
    }

    private ZoneId resolveZoneId() {
        String zone = garageProperties != null ? garageProperties.getTimezone() : null;
        if (zone == null || zone.isBlank()) {
            return ZoneId.of("UTC");
        }
        return ZoneId.of(zone);
    }

    private List<TimeRange> resolveOpeningRanges(LocalDate date, List<GarageOpeningHour> hours) {
        List<GarageOpeningHour> exceptional = hours.stream()
                .filter(h -> h.getScope() == GarageOpeningHourScope.EXCEPTIONAL)
                .filter(h -> isDateInExceptionalRange(date, h))
                .toList();

        List<GarageOpeningHour> relevant = exceptional.isEmpty()
                ? hours.stream()
                        .filter(h -> h.getScope() == GarageOpeningHourScope.ANNUAL)
                        .filter(h -> h.getDayOfWeek() != null && h.getDayOfWeek().equals(date.getDayOfWeek()))
                        .toList()
                : exceptional;

        if (relevant.stream().anyMatch(h -> h.getStatus() == GarageOpeningHourStatus.CLOSED)) {
            return List.of();
        }

        return relevant.stream()
                .flatMap(this::toRanges)
                .sorted(Comparator.comparing(TimeRange::start))
                .toList();
    }

    private boolean isDateInExceptionalRange(LocalDate date, GarageOpeningHour hour) {
        if (hour.getExceptionalType() == GarageOpeningHourExceptionalType.SINGLE_DAY) {
            return hour.getExceptionalDate() != null && hour.getExceptionalDate().equals(date);
        }
        if (hour.getExceptionalType() == GarageOpeningHourExceptionalType.PERIOD) {
            LocalDate start = hour.getExceptionalStartDate();
            LocalDate end = hour.getExceptionalEndDate();
            if (start == null || end == null) {
                return false;
            }
            return !date.isBefore(start) && !date.isAfter(end);
        }
        return false;
    }

    private Stream<TimeRange> toRanges(GarageOpeningHour hour) {
        if (hour.getStatus() != GarageOpeningHourStatus.OPEN) {
            return Stream.empty();
        }
        if (hour.getOpeningType() == GarageOpeningHourType.CONTINUOUS) {
            return Stream.of(new TimeRange(hour.getStartTime(), hour.getEndTime()));
        }
        if (hour.getOpeningType() == GarageOpeningHourType.SPLIT) {
            return Stream.of(
                    new TimeRange(hour.getStartTime(), hour.getEndTime()),
                    new TimeRange(hour.getStartTime2(), hour.getEndTime2())
            );
        }
        return Stream.empty();
    }

    private record TimeRange(LocalTime start, LocalTime end) { }

    private record SlotKey(Instant start, Instant end) { }

    private record SlotAvailability(
            StatutCreneau statut,
            Integer totalCount,
            Integer availableCount,
            Integer reservedCount,
            Integer unavailableCount
    ) {
        private static SlotAvailability open(StatutCreneau statut,
                                             Integer total,
                                             Integer available,
                                             Integer reserved,
                                             Integer unavailable) {
            return new SlotAvailability(statut, total, available, reserved, unavailable);
        }

        private static SlotAvailability closed(StatutCreneau statut) {
            return new SlotAvailability(statut, 0, 0, 0, 0);
        }
    }

    private Integer resolveSlotId(List<Creneau> creneaux) {
        if (creneaux == null || creneaux.isEmpty()) {
            return null;
        }
        return creneaux.stream()
                .filter(creneau -> isStatut(creneau, STATUT_RESERVE))
                .map(Creneau::getIdCreneau)
                .filter(id -> id != null && id > 0)
                .min(Integer::compareTo)
                .or(() -> creneaux.stream()
                        .map(Creneau::getIdCreneau)
                        .filter(id -> id != null && id > 0)
                        .min(Integer::compareTo))
                .orElse(null);
    }
}
