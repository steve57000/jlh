package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.GarageOpeningHourRequest;
import com.jlh.jlhautopambackend.dto.GarageOpeningHourResponse;
import com.jlh.jlhautopambackend.mapper.GarageOpeningHourMapper;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHour;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourExceptionalType;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourScope;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourStatus;
import com.jlh.jlhautopambackend.modeles.GarageOpeningHourType;
import com.jlh.jlhautopambackend.repository.GarageOpeningHourRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GarageOpeningHourServiceImpl implements GarageOpeningHourService {

    private final GarageOpeningHourRepository repository;
    private final GarageOpeningHourMapper mapper;

    public GarageOpeningHourServiceImpl(GarageOpeningHourRepository repository,
                                        GarageOpeningHourMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GarageOpeningHourResponse> findAll() {
        return repository.findAll(Sort.by("scope", "dayOfWeek", "exceptionalDate", "exceptionalStartDate"))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GarageOpeningHourResponse> findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    public GarageOpeningHourResponse create(GarageOpeningHourRequest request) {
        validateRequest(request);
        GarageOpeningHour entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public Optional<GarageOpeningHourResponse> update(Integer id, GarageOpeningHourRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    validateRequest(request);
                    applyUpdate(existing, request);
                    return mapper.toResponse(repository.save(existing));
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

    private void applyUpdate(GarageOpeningHour entity, GarageOpeningHourRequest request) {
        entity.setScope(request.getScope());
        entity.setStatus(request.getStatus());
        entity.setOpeningType(request.getOpeningType());
        entity.setDayOfWeek(request.getDayOfWeek());
        entity.setExceptionalType(request.getExceptionalType());
        entity.setExceptionalDate(request.getExceptionalDate());
        entity.setExceptionalStartDate(request.getExceptionalStartDate());
        entity.setExceptionalEndDate(request.getExceptionalEndDate());
        entity.setLabel(request.getLabel());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setStartTime2(request.getStartTime2());
        entity.setEndTime2(request.getEndTime2());
    }

    private void validateRequest(GarageOpeningHourRequest request) {
        if (request.getScope() == null) {
            throw new IllegalArgumentException("Le champ scope est requis.");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Le champ status est requis.");
        }
        validateScope(request);
        validateStatusAndTimes(request);
    }

    private void validateScope(GarageOpeningHourRequest request) {
        if (request.getScope() == GarageOpeningHourScope.ANNUAL) {
            if (request.getDayOfWeek() == null) {
                throw new IllegalArgumentException("Le jour de la semaine est requis pour un horaire annuel.");
            }
            if (request.getExceptionalType() != null
                    || request.getExceptionalDate() != null
                    || request.getExceptionalStartDate() != null
                    || request.getExceptionalEndDate() != null
                    || (request.getLabel() != null && !request.getLabel().isBlank())) {
                throw new IllegalArgumentException("Les champs exceptionnels ne sont pas autorisés pour un horaire annuel.");
            }
        } else {
            if (request.getExceptionalType() == null) {
                throw new IllegalArgumentException("Le type exceptionnel est requis pour un horaire exceptionnel.");
            }
            if (request.getExceptionalType() == GarageOpeningHourExceptionalType.SINGLE_DAY) {
                if (request.getExceptionalDate() == null) {
                    throw new IllegalArgumentException("La date est requise pour un horaire exceptionnel unique.");
                }
                if (request.getExceptionalStartDate() != null || request.getExceptionalEndDate() != null) {
                    throw new IllegalArgumentException("La période ne doit pas être renseignée pour un jour unique.");
                }
            } else if (request.getExceptionalType() == GarageOpeningHourExceptionalType.PERIOD) {
                if (request.getExceptionalStartDate() == null || request.getExceptionalEndDate() == null) {
                    throw new IllegalArgumentException("Les dates de début et fin sont requises pour une période exceptionnelle.");
                }
                LocalDate start = request.getExceptionalStartDate();
                LocalDate end = request.getExceptionalEndDate();
                if (start.isAfter(end)) {
                    throw new IllegalArgumentException("La date de début doit précéder la date de fin.");
                }
                if (request.getLabel() == null || request.getLabel().isBlank()) {
                    throw new IllegalArgumentException("Un libellé est requis pour une période exceptionnelle.");
                }
            }
            if (request.getDayOfWeek() != null) {
                throw new IllegalArgumentException("Le jour de la semaine ne doit pas être renseigné pour un horaire exceptionnel.");
            }
        }
    }

    private void validateStatusAndTimes(GarageOpeningHourRequest request) {
        if (request.getStatus() == GarageOpeningHourStatus.CLOSED) {
            if (request.getOpeningType() != null
                    || request.getStartTime() != null
                    || request.getEndTime() != null
                    || request.getStartTime2() != null
                    || request.getEndTime2() != null) {
                throw new IllegalArgumentException("Aucun horaire ne doit être renseigné si le garage est fermé.");
            }
            return;
        }

        if (request.getOpeningType() == null) {
            throw new IllegalArgumentException("Le type d'ouverture est requis lorsque le garage est ouvert.");
        }

        if (request.getOpeningType() == GarageOpeningHourType.CONTINUOUS) {
            requireTimeRange(request.getStartTime(), request.getEndTime(), "horaire continu");
            if (request.getStartTime2() != null || request.getEndTime2() != null) {
                throw new IllegalArgumentException("Les horaires secondaires ne sont pas autorisés pour un horaire continu.");
            }
        } else if (request.getOpeningType() == GarageOpeningHourType.SPLIT) {
            requireTimeRange(request.getStartTime(), request.getEndTime(), "horaire du matin");
            requireTimeRange(request.getStartTime2(), request.getEndTime2(), "horaire de l'après-midi");
        }
    }

    private void requireTimeRange(LocalTime start, LocalTime end, String label) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Les heures de " + label + " doivent être renseignées.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("L'heure de début doit être avant l'heure de fin (" + label + ").");
        }
    }
}
