package com.goodleap.healthtracker.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("health_events")
public class HealthEvent {
    @Id private String id;
    @Indexed private String serviceId;
    private Instant checkedAt;
    private long responseTimeMs;
    private int httpStatus;
    private ServiceStatus status;

    public HealthEvent() {}
    public HealthEvent(String serviceId, Instant checkedAt, long responseTimeMs, int httpStatus, ServiceStatus status) {
        this.serviceId = serviceId; this.checkedAt = checkedAt; this.responseTimeMs = responseTimeMs; this.httpStatus = httpStatus; this.status = status;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public int getHttpStatus() { return httpStatus; }
    public void setHttpStatus(int httpStatus) { this.httpStatus = httpStatus; }
    public ServiceStatus getStatus() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }
}
