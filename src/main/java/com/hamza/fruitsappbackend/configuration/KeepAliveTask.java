package com.hamza.fruitsappbackend.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveTask {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveTask.class);

    private static final String API_URL = "https://fruitappbackendspringbootrestfullapijava.onrender.com/ping";

    private final RestTemplate restTemplate;

    public KeepAliveTask() {
        this.restTemplate = new RestTemplate();
    }


    @Scheduled(fixedRate = 300000)
    public void pingApi() {
        try {
            String response = restTemplate.getForObject(API_URL, String.class);
            logger.info("Successfully pinged API: {}", response);
        } catch (Exception e) {
            logger.error("Failed to ping API", e);
        }
    }
}
