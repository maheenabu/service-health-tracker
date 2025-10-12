package com.goodleap.healthtracker;

import com.goodleap.healthtracker.security.TokenFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServiceHealthTrackerApplication {
    public static void main(String[] args) {
         SpringApplication.run(ServiceHealthTrackerApplication.class, args);
    }

    @Bean CommandLineRunner printSampleToken() { return args -> {
        System.out.println("==== SAMPLE ADMIN JWT ====");
        System.out.println(TokenFactory.sampleAdminToken());
        System.out.println("==========================");
    }; }
}
