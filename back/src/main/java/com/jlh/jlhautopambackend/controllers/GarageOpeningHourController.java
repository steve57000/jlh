package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.GarageOpeningHourRequest;
import com.jlh.jlhautopambackend.dto.GarageOpeningHourResponse;
import com.jlh.jlhautopambackend.services.GarageOpeningHourService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/garage-hours")
public class GarageOpeningHourController {

    private final GarageOpeningHourService service;

    public GarageOpeningHourController(GarageOpeningHourService service) {
        this.service = service;
    }

    @GetMapping
    public List<GarageOpeningHourResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GarageOpeningHourResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_PRINCIPAL')")
    public ResponseEntity<GarageOpeningHourResponse> create(@RequestBody GarageOpeningHourRequest request) {
        GarageOpeningHourResponse response = service.create(request);
        URI location = URI.create("/api/garage-hours/" + response.getIdOpeningHour());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_PRINCIPAL')")
    public ResponseEntity<GarageOpeningHourResponse> update(@PathVariable Integer id,
                                                            @RequestBody GarageOpeningHourRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_PRINCIPAL')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().build();
    }
}
