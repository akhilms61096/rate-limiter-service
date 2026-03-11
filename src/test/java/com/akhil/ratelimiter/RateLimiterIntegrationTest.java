package com.akhil.ratelimiter;

import com.akhil.ratelimiter.service.LocalRateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "rate-limiter.capacity=3")
@AutoConfigureMockMvc
class RateLimiterIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldReturn429AfterBurstExhausted() throws Exception {
        // First 3 should pass
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/ping").header("X-API-Key", "test-client-it"))
                    .andExpect(status().isOk());
        }
        // 4th should be rate limited
        mockMvc.perform(get("/api/ping").header("X-API-Key", "test-client-it"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("Retry-After"));
    }
}
