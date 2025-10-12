package com.goodleap.healthtracker.service;

import com.goodleap.healthtracker.model.MonitoredService;
import com.goodleap.healthtracker.model.ServiceStatus;
import com.goodleap.healthtracker.repo.MonitoredServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {
    private final MonitoredServiceRepository services;

    public AlertService(MonitoredServiceRepository services) {
        this.services = services;
    }

    public List<MonitoredService> findUnhealthyOrUnknown() {
        return services.findAll().stream()
                .filter(s -> s.getStatus() != ServiceStatus.HEALTHY)
                .toList();
    }
}
