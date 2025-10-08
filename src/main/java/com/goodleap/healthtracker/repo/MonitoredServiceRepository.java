package com.goodleap.healthtracker.repo;
import com.goodleap.healthtracker.model.MonitoredService;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MonitoredServiceRepository extends MongoRepository<MonitoredService, String> {
    Optional<MonitoredService> findByName(String name);
}
