package com.akhil.ratelimiter.controller;

import com.akhil.ratelimiter.service.RateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RateLimitController {

    private final RateLimiterService rateLimiterService;

    public RateLimitController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/status/{clientId}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String clientId) {
        long tokens = rateLimiterService.getAvailableTokens(clientId);
        return ResponseEntity.ok(Map.of(
                "clientId", clientId,
                "availableTokens", tokens
        ));
    }
}
