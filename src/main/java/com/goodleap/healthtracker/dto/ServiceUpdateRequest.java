package com.goodleap.healthtracker.dto;

import jakarta.validation.constraints.Min;
import java.util.Map;

public class ServiceUpdateRequest {
    public String name;
    public String version;
    public String baseUrl;
    @Min(1) public Integer checkIntervalSeconds;
    public Map<String, String> headers;
}
