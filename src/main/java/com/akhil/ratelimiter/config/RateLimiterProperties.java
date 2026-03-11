package com.akhil.ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {
    private long capacity = 10;
    private long refillRatePerSecond = 5;
    private String mode = "local"; // local | redis
}
