package com.goodleap.healthtracker.controller;

import com.goodleap.healthtracker.dto.ServiceCreateRequest;
import com.goodleap.healthtracker.dto.ServiceUpdateRequest;
import com.goodleap.healthtracker.model.MonitoredService;
import com.goodleap.healthtracker.service.ServiceRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Service Health Tracker")
public class ServiceController {

    @Autowired
    private  ServiceRegistryService registry;

    @Operation(summary = "Register a new service to be monitored")
    @PostMapping("/services")
    public ResponseEntity<MonitoredService> create(@Valid @RequestBody ServiceCreateRequest req) {
        var created = registry.create(req);
        return ResponseEntity.created(created.location()).body(created.body());
    }

    @Operation(summary = "List all services with status")
    @GetMapping("/services")
    public List<MonitoredService> all() {
        return registry.list();
    }

    @Operation(summary = "Get one service with recent health events (last 20)")
    @GetMapping("/services/{id}")
    public Map<String, Object> one(@PathVariable("id") String id) {
        return registry.getWithRecent(id, 20);
    }

    @Operation(summary = "Patch service metadata")
    @PatchMapping("/services/{id}")
    public MonitoredService patch(@PathVariable("id") String id, @RequestBody ServiceUpdateRequest req) {
        return registry.patch(id, req);
    }

    @Operation(summary = "Delete/unregister a service")
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        boolean deleted = registry.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
