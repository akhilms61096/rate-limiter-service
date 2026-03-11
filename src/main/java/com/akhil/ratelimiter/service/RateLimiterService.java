package com.akhil.ratelimiter.service;

public interface RateLimiterService {
    boolean tryAcquire(String clientId);
    long getAvailableTokens(String clientId);
}
