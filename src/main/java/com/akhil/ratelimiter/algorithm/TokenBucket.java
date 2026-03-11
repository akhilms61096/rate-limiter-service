package com.akhil.ratelimiter.algorithm;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Token bucket: refill happens lazily on each tryConsume call.
 * Interview angle: why lazy refill over background thread?
 *   — no contention, no timer drift, O(1) per request.
 */
public class TokenBucket {

    private final long capacity;
    private final double refillRatePerNano; // tokens per nanosecond

    private final AtomicLong availableTokens;
    private volatile long lastRefillTimestamp;

    public TokenBucket(long capacity, long refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerNano = (double) refillRatePerSecond / 1_000_000_000L;
        this.availableTokens = new AtomicLong(capacity);
        this.lastRefillTimestamp = System.nanoTime();
    }

    public synchronized boolean tryConsume(long tokens) {
        refill();
        long current = availableTokens.get();
        if (current < tokens) {
            return false;
        }
        availableTokens.addAndGet(-tokens);
        return true;
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsed = now - lastRefillTimestamp;
        long tokensToAdd = (long) (elapsed * refillRatePerNano);
        if (tokensToAdd > 0) {
            long newTokens = Math.min(capacity, availableTokens.get() + tokensToAdd);
            availableTokens.set(newTokens);
            lastRefillTimestamp = now;
        }
    }

    public long getAvailableTokens() {
        refill();
        return availableTokens.get();
    }
}
