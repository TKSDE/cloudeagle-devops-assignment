package com.cloudeagle.syncservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SyncServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncServiceApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "Welcome to CloudEagle Sync Service API! The application is running successfully.";
    }

}
