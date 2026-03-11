# Rate Limiter Service

Token Bucket rate limiter in Java 21 + Spring Boot 3.x.  
Two modes: **local** (in-memory, single-node) and **redis** (distributed, Lua-atomic).

## Architecture

```
Request → RateLimitFilter → RateLimiterService (local | redis)
                                    ↓
                             TokenBucket (in-memory)
                             OR Redis + Lua script
```

### Why Token Bucket over Leaky Bucket / Fixed Window?
- **Fixed window** has edge-case burst: 2x rate at window boundary
- **Leaky bucket** smooths traffic but penalises legitimate burst (e.g. mobile reconnect)
- **Token bucket** allows controlled burst up to `capacity` while enforcing sustained rate — best of both

### Why Lua for Redis mode?
Redis executes Lua scripts atomically. Without it, a `GET → compute → SET` sequence has a race condition under concurrent requests across multiple app nodes. Lua removes the need for `WATCH/MULTI/EXEC` optimistic locking.

### Why lazy refill over a background timer?
- No background thread contention
- No timer drift across distributed nodes
- O(1) per request — refill cost is absorbed into the acquire call

## Running

### Local mode (no Redis needed)
```bash
mvn spring-boot:run
```

### Redis distributed mode
```bash
docker-compose up
```

## API

| Endpoint | Description |
|----------|-------------|
| `GET /api/ping` | Test endpoint — rate limited |
| `GET /api/status/{clientId}` | Check available tokens for a client |
| `GET /actuator/health` | Health check (not rate limited) |

## Client identification priority
1. `X-API-Key` header
2. `X-Forwarded-For` header (first IP)
3. Remote IP

## Response headers
- `X-RateLimit-Remaining` — tokens left after this request
- `Retry-After` — seconds until retry (on 429)

## Interview talking points
- Token bucket vs sliding window log vs sliding window counter
- Distributed atomicity: why Lua > WATCH/MULTI/EXEC
- Clock skew handling in distributed mode
- `computeIfAbsent` thread safety for ConcurrentHashMap
- Extending to per-route or tiered limits (free/pro/enterprise)
