-- Token Bucket in Lua (atomic execution in Redis)
-- KEYS[1] = rate limit key
-- ARGV[1] = capacity, ARGV[2] = refill_rate/sec, ARGV[3] = now_ms, ARGV[4] = requested tokens

local key        = KEYS[1]
local capacity   = tonumber(ARGV[1])
local rate       = tonumber(ARGV[2])  -- tokens per second
local now        = tonumber(ARGV[3])  -- milliseconds
local requested  = tonumber(ARGV[4])

local data = redis.call("HMGET", key, "tokens", "last_refill")

local tokens     = tonumber(data[1]) or capacity
local last_refill = tonumber(data[2]) or now

-- Lazy refill: calculate tokens earned since last call
local elapsed_sec = (now - last_refill) / 1000.0
local refill      = math.floor(elapsed_sec * rate)
tokens = math.min(capacity, tokens + refill)

local allowed = 0
if tokens >= requested then
    tokens  = tokens - requested
    allowed = 1
end

-- TTL = 2x the time to fully refill from zero
local ttl_sec = math.ceil((capacity / rate) * 2)
redis.call("HMSET", key, "tokens", tokens, "last_refill", now)
redis.call("EXPIRE", key, ttl_sec)

return { allowed, tokens }
