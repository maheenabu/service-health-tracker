package com.goodleap.healthtracker.controller;

import com.goodleap.healthtracker.model.MonitoredService;
import com.goodleap.healthtracker.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Alert Service Health Tracker")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Operation(summary = "List services that are UNHEALTHY or UNKNOWN")
    @GetMapping("/alerts")
    public List<MonitoredService> alerts() {
        return alertService.findUnhealthyOrUnknown();
    }
}
