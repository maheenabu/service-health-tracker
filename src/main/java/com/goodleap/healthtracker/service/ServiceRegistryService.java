package com.goodleap.healthtracker.service;

import com.goodleap.healthtracker.dto.ServiceCreateRequest;
import com.goodleap.healthtracker.dto.ServiceUpdateRequest;
import com.goodleap.healthtracker.model.HealthEvent;
import com.goodleap.healthtracker.model.MonitoredService;
import com.goodleap.healthtracker.repo.HealthEventRepository;
import com.goodleap.healthtracker.repo.MonitoredServiceRepository;
import com.goodleap.healthtracker.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class ServiceRegistryService {

    @Autowired
    private MonitoredServiceRepository services;
    @Autowired
    private HealthEventRepository events;

    public Created<MonitoredService> create(ServiceCreateRequest req) {
        MonitoredService s = new MonitoredService();
        s.setName(req.name);
        s.setVersion(req.version);
        s.setBaseUrl(req.baseUrl);
        s.setCheckIntervalSeconds(req.checkIntervalSeconds != null ? req.checkIntervalSeconds : 60);
        s.setHeaders(req.headers);
        s.setLastCheckedAt(null);
        MonitoredService saved = services.save(s);
        return new Created<>(saved, URI.create("/api/v1/services/" + saved.getId()));
    }

    /** Fetch all services. */
    public List<MonitoredService> list() {
        return services.findAll();
    }

    /** Get one service plus its recent events. */
    public Map<String, Object> getWithRecent(String id, int recentCount) {
        MonitoredService svc = services.findById(id)
                .orElseThrow(() -> new NotFoundException("Service " + id + " not found"));
        List<HealthEvent> recent = events.findByServiceIdOrderByCheckedAtDesc(id, PageRequest.of(0, recentCount));
        return Map.of("service", svc, "recent", recent);
    }

    /** Patch/Update service. */
    public MonitoredService patch(String id, ServiceUpdateRequest req) {
        MonitoredService svc = services.findById(id)
                .orElseThrow(() -> new NotFoundException("Service " + id + " not found"));
        if (req.name != null) svc.setName(req.name);
        if (req.version != null) svc.setVersion(req.version);
        if (req.baseUrl != null) svc.setBaseUrl(req.baseUrl);
        if (req.checkIntervalSeconds != null) svc.setCheckIntervalSeconds(req.checkIntervalSeconds);
        if (req.headers != null) svc.setHeaders(req.headers);
        svc.setLastCheckedAt(svc.getLastCheckedAt() != null ? svc.getLastCheckedAt() : null);
        return services.save(svc);
    }

    /** Delete service by id. */
    public boolean delete(String id) {
        return services.findById(id).map(svc -> {
            services.deleteById(id);
            return true;
        }).orElse(false);
    }

    // Small value object to return Location alongside resource.
    public record Created<T>(T body, URI location) {}
}
