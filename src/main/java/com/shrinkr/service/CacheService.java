package com.shrinkr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.redis.ttl-minutes:60}")
    private long ttlMinutes;

    private static final String KEY_PREFIX = "url:";

    public void cacheUrl(String shortCode, String longUrl) {
        String key = KEY_PREFIX + shortCode;
        redisTemplate.opsForValue().set(key, longUrl, Duration.ofMinutes(ttlMinutes));
        log.info("Cached shortCode={} in Redis", shortCode);
    }

    public Optional<String> getLongUrl(String shortCode) {
        String key = KEY_PREFIX + shortCode;
        String value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            log.info("Cache HIT for shortCode={}", shortCode);
            return Optional.of(value);
        }

        log.info("Cache MISS for shortCode={}", shortCode);
        return Optional.empty();
    }

    public void evict(String shortCode) {
        redisTemplate.delete(KEY_PREFIX + shortCode);
    }
}
