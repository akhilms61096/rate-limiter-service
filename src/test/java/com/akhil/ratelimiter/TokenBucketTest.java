package com.akhil.ratelimiter;

import com.akhil.ratelimiter.algorithm.TokenBucket;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketTest {

    @Test
    void shouldAllowRequestsUpToCapacity() {
        TokenBucket bucket = new TokenBucket(5, 1);
        for (int i = 0; i < 5; i++) {
            assertThat(bucket.tryConsume(1)).isTrue();
        }
        assertThat(bucket.tryConsume(1)).isFalse();
    }

    @Test
    void shouldRefillOverTime() throws InterruptedException {
        TokenBucket bucket = new TokenBucket(5, 5); // 5 tokens/sec
        for (int i = 0; i < 5; i++) bucket.tryConsume(1);
        assertThat(bucket.tryConsume(1)).isFalse();

        Thread.sleep(1100); // wait ~1 second to refill ~5 tokens
        assertThat(bucket.tryConsume(1)).isTrue();
    }

    @Test
    void shouldNotExceedCapacityOnRefill() throws InterruptedException {
        TokenBucket bucket = new TokenBucket(5, 100); // fast refill
        bucket.tryConsume(2);
        Thread.sleep(500);
        assertThat(bucket.getAvailableTokens()).isLessThanOrEqualTo(5);
    }
}
