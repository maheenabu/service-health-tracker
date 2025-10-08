package com.goodleap.healthtracker.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class ServiceCreateRequest {
    @NotBlank public String name;
    public String version;
    @NotBlank public String baseUrl;
    @Min(1) public Integer checkIntervalSeconds = 60;
    public Map<String, String> headers;
}
