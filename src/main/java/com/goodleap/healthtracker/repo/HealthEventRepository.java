package com.goodleap.healthtracker.repo;
import com.goodleap.healthtracker.model.HealthEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface HealthEventRepository extends MongoRepository<HealthEvent, String> {
    List<HealthEvent> findByServiceIdOrderByCheckedAtDesc(String serviceId, Pageable pageable);
}
