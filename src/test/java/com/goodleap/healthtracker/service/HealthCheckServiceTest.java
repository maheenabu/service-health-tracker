package com.goodleap.healthtracker.service;
import com.goodleap.healthtracker.model.MonitoredService;
import com.goodleap.healthtracker.repo.HealthEventRepository;
import com.goodleap.healthtracker.repo.MonitoredServiceRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import java.io.IOException;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HealthCheckServiceTest {

    MockWebServer server;

    @BeforeEach void setup() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach void cleanup() throws IOException {
        server.shutdown();
    }

    @Test void marksHealthyOn200() {

        server.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));

        var svcRepo = Mockito.mock(MonitoredServiceRepository.class);
        var evRepo = Mockito.mock(HealthEventRepository.class);

        var svc = new MonitoredService();
        svc.setId("1");
        svc.setName("demo");
        svc.setBaseUrl(server.url("/health").toString());
        svc.setCheckIntervalSeconds(1);

        when(svcRepo.findAll()).thenReturn(List.of(svc));
        when(svcRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var hc = new HealthCheckService(svcRepo, evRepo);
        hc.tick();

        verify(svcRepo, atLeastOnce()).save(any()); verify(evRepo, atLeastOnce()).save(any());
    }

    @Test void marksUnhealthyOn500() {

        server.enqueue(new MockResponse().setResponseCode(500).setBody("boom"));

        var svcRepo = Mockito.mock(MonitoredServiceRepository.class);
        var evRepo = Mockito.mock(HealthEventRepository.class);

        var svc = new MonitoredService();
        svc.setId("1");
        svc.setName("demo");
        svc.setBaseUrl(server.url("/health").toString());
        svc.setCheckIntervalSeconds(1);

        when(svcRepo.findAll()).thenReturn(List.of(svc));
        when(svcRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var hc = new HealthCheckService(svcRepo, evRepo);
        hc.tick();

        verify(svcRepo, atLeastOnce()).save(any()); verify(evRepo, atLeastOnce()).save(any());
    }
}
