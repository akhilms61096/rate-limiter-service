package com.akhil.ratelimiter.service;

import com.akhil.ratelimiter.config.RateLimiterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Distributed rate limiter using Redis + Lua script for atomic token bucket.
 * Interview angle: why Lua? Redis executes scripts atomically — no WATCH/MULTI/EXEC
 * needed, and no race condition between GET and SET across nodes.
 */
@Service
@ConditionalOnProperty(name = "rate-limiter.mode", havingValue = "redis")
public class RedisRateLimiterService implements RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final RateLimiterProperties props;
    private final RedisScript<List<Long>> rateLimitScript;

    public RedisRateLimiterService(StringRedisTemplate redisTemplate, RateLimiterProperties props) {
        this.redisTemplate = redisTemplate;
        this.props = props;
        this.rateLimitScript = RedisScript.of(
                new ClassPathResource("scripts/token_bucket.lua"), (Class<List<Long>>) (Class<?>) List.class);
    }

    @Override
    public boolean tryAcquire(String clientId) {
        String key = "rate_limit:" + clientId;
        List<Long> result = redisTemplate.execute(
                rateLimitScript,
                List.of(key),
                String.valueOf(props.getCapacity()),
                String.valueOf(props.getRefillRatePerSecond()),
                String.valueOf(System.currentTimeMillis()),
                "1"
        );
        return result != null && result.get(0) == 1L;
    }

    @Override
    public long getAvailableTokens(String clientId) {
        String key = "rate_limit:" + clientId;
        String val = redisTemplate.opsForHash().get(key, "tokens") != null
                ? (String) redisTemplate.opsForHash().get(key, "tokens")
                : String.valueOf(props.getCapacity());
        return Long.parseLong(val);
    }
}
