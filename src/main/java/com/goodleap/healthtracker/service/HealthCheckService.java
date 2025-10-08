package com.goodleap.healthtracker.service;
import com.goodleap.healthtracker.model.HealthEvent;
import com.goodleap.healthtracker.model.MonitoredService;
import com.goodleap.healthtracker.model.ServiceStatus;
import com.goodleap.healthtracker.repo.HealthEventRepository;
import com.goodleap.healthtracker.repo.MonitoredServiceRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service @EnableScheduling
public class HealthCheckService {
    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);
    private final MonitoredServiceRepository services;
    private final HealthEventRepository events;
    private final RestClient client;

    public HealthCheckService(MonitoredServiceRepository services, HealthEventRepository events) {
        this.services = services;
        this.events = events;

        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(java.time.Duration.ofSeconds(2));
        rf.setReadTimeout(java.time.Duration.ofSeconds(5));

        this.client = RestClient.builder()
                .requestFactory(rf)
                .build();
    }

    @Scheduled(fixedRateString = "${health.check.tick-ms:60000}")
    public void tick() {
        Instant now = Instant.now();
        for (MonitoredService s : services.findAll()) {
            if (s.getLastCheckedAt() == null || s.getLastCheckedAt().plusSeconds(Math.max(1, s.getCheckIntervalSeconds())).isBefore(now)) {
                performCheck(s);
            }
        }
    }
    @Retry(name = "healthCheckRetry", fallbackMethod = "fallback")
    @CircuitBreaker(name = "healthCheckCB", fallbackMethod = "fallback")
    public void performCheck(MonitoredService s) {
        Instant start = Instant.now();
        int code; ServiceStatus status;
        try {
            HttpHeaders headers = new HttpHeaders();
            Map<String, String> hdrs = s.getHeaders(); if (hdrs != null) hdrs.forEach(headers::add);
            URI uri = UriComponentsBuilder.fromHttpUrl(s.getBaseUrl()).build().toUri();
            ResponseEntity<String> resp = client.get().uri(uri).headers(h -> h.addAll(headers)).retrieve().toEntity(String.class);
            code = resp.getStatusCode().value();
            status = (code == 200) ? ServiceStatus.HEALTHY : ServiceStatus.UNHEALTHY;
        } catch (Exception e) {
            log.debug("Health check failed for {}: {}", s.getName(), e.getMessage());
            code = 0; status = ServiceStatus.UNHEALTHY;
        }
        record(s, start, code, status);
    }

    private void fallback(MonitoredService s, Throwable t) {
        Instant start = Instant.now();
        record(s, start, 0, ServiceStatus.UNHEALTHY);
    }

    private void record(MonitoredService s, Instant start, int httpCode, ServiceStatus status) {
        long ms = Duration.between(start, Instant.now()).toMillis();
        s.setLastCheckedAt(Instant.now()); s.setLastResponseTimeMs(ms); s.setLastHttpStatus(httpCode); s.setStatus(status);
        services.save(s);
        events.save(new HealthEvent(s.getId(), s.getLastCheckedAt(), ms, httpCode, status));
    }

    public List<MonitoredService> findUnhealthyOrUnknown() {
        return services.findAll().stream().filter(s -> s.getStatus() != ServiceStatus.HEALTHY).toList();
    }
}
