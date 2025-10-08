package com.goodleap.healthtracker.controller;
import com.goodleap.healthtracker.dto.ServiceCreateRequest;
import com.goodleap.healthtracker.dto.ServiceUpdateRequest;
import com.goodleap.healthtracker.model.HealthEvent;
import com.goodleap.healthtracker.model.MonitoredService;
import com.goodleap.healthtracker.repo.HealthEventRepository;
import com.goodleap.healthtracker.repo.MonitoredServiceRepository;
import com.goodleap.healthtracker.service.HealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/v1")
@Tag(name = "Service Health Tracker")
public class ServiceController {

    private final MonitoredServiceRepository repo;
    private final HealthEventRepository events;
    private final HealthCheckService health;

    public ServiceController(MonitoredServiceRepository repo,
                             HealthEventRepository events,
                             HealthCheckService health) {
        this.repo = repo;
        this.events = events;
        this.health = health;
    }

    @Operation(summary = "Register a new service to be monitored")
    @PostMapping("/services")
    public ResponseEntity<MonitoredService> create(@Valid @RequestBody ServiceCreateRequest req) {
        MonitoredService s = new MonitoredService();
        s.setName(req.name); s.setVersion(req.version); s.setBaseUrl(req.baseUrl);
        s.setCheckIntervalSeconds(req.checkIntervalSeconds != null ? req.checkIntervalSeconds : 60);
        s.setHeaders(req.headers);
        MonitoredService saved = repo.save(s);
        return ResponseEntity.created(URI.create("/api/v1/services/" + saved.getId())).body(saved);
    }

    @Operation(summary = "List all services with status")
    @GetMapping("/services") public List<MonitoredService> all() {
        return repo.findAll();
    }

    @Operation(summary = "Get one service with recent health events (last 20)")
    @GetMapping("/services/{id}")
    public ResponseEntity<?> one(@PathVariable String id) {
        return repo.findById(id).<ResponseEntity<?>>map(svc -> {
            List<HealthEvent> recent = events.findByServiceIdOrderByCheckedAtDesc(id, PageRequest.of(0,20));
            return ResponseEntity.ok(Map.of("service", svc, "recent", recent));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Patch service metadata")
    @PatchMapping("/services/{id}")
    public ResponseEntity<MonitoredService> patch(@PathVariable String id, @RequestBody ServiceUpdateRequest req) {
        return repo.findById(id).map(svc -> {
            if (req.name != null) svc.setName(req.name);
            if (req.version != null) svc.setVersion(req.version);
            if (req.baseUrl != null) svc.setBaseUrl(req.baseUrl);
            if (req.checkIntervalSeconds != null) svc.setCheckIntervalSeconds(req.checkIntervalSeconds);
            if (req.headers != null) svc.setHeaders(req.headers);
            return ResponseEntity.ok(repo.save(svc));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete/unregister a service")
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (repo.findById(id).isPresent()) {
            repo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "List services that are UNHEALTHY or UNKNOWN")
    @GetMapping("/alerts") public List<MonitoredService> alerts() {
        return health.findUnhealthyOrUnknown();
    }
}
