package com.hamza.fruitsappbackend.configuration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveTask {

    private static final String API_URL = "https://fruitappbackendspringbootrestfullapijava.onrender.com/ping";

    private final RestTemplate restTemplate;

    public KeepAliveTask() {
        this.restTemplate = new RestTemplate();
    }


    @Scheduled(fixedRate = 300000)
    public void pingApi() {
        try {
            restTemplate.getForObject(API_URL, String.class);
        } catch (Exception e) {
            System.out.println("Sending ping request to keep service alive");
        }
    }
}
