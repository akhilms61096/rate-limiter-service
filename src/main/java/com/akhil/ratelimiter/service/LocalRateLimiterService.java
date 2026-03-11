package com.akhil.ratelimiter.service;

import com.akhil.ratelimiter.algorithm.TokenBucket;
import com.akhil.ratelimiter.config.RateLimiterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "rate-limiter.mode", havingValue = "local", matchIfMissing = true)
public class LocalRateLimiterService implements RateLimiterService {

    private final RateLimiterProperties props;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public LocalRateLimiterService(RateLimiterProperties props) {
        this.props = props;
    }

    @Override
    public boolean tryAcquire(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId,
                id -> new TokenBucket(props.getCapacity(), props.getRefillRatePerSecond()));
        return bucket.tryConsume(1);
    }

    @Override
    public long getAvailableTokens(String clientId) {
        TokenBucket bucket = buckets.get(clientId);
        return bucket == null ? props.getCapacity() : bucket.getAvailableTokens();
    }
}
