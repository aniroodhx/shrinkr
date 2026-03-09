package com.shrinkr.service;

import com.shrinkr.dto.ShortenRequest;
import com.shrinkr.dto.ShortenResponse;
import com.shrinkr.exception.UrlNotFoundException;
import com.shrinkr.model.Url;
import com.shrinkr.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;
    private final CacheService cacheService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ShortenResponse shortenUrl(ShortenRequest request) {
        String longUrl = request.getLongUrl().trim();

        // Idempotency — same URL always returns same short code
        Optional<Url> existing = urlRepository.findByLongUrl(longUrl);
        if (existing.isPresent()) {
            log.info("URL already exists, returning existing mapping");
            return buildResponse(existing.get());
        }

        // Save with placeholder to get DB-generated ID
        Url url = Url.builder()
                .longUrl(longUrl)
                .shortCode("PENDING")
                .clickCount(0L)
                .build();
        url = urlRepository.save(url);

        // Use the ID to generate Base62 short code
        String shortCode = base62Encoder.encode(url.getId());
        log.info("Generated shortCode={} for ID={}", shortCode, url.getId());

        // Update with real short code
        url.setShortCode(shortCode);
        url = urlRepository.save(url);

        // Cache it
        cacheService.cacheUrl(shortCode, longUrl);

        return buildResponse(url);
    }

    @Transactional
    public String getLongUrl(String shortCode) {
        // Fast path — Redis
        Optional<String> cached = cacheService.getLongUrl(shortCode);
        if (cached.isPresent()) {
            urlRepository.incrementClickCount(shortCode);
            return cached.get();
        }

        // Slow path — DB
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                        "No URL found for short code: " + shortCode
                ));

        // Re-populate cache
        cacheService.cacheUrl(shortCode, url.getLongUrl());
        urlRepository.incrementClickCount(shortCode);

        return url.getLongUrl();
    }

    public ShortenResponse getUrlStats(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                        "No URL found for short code: " + shortCode
                ));
        return buildResponse(url);
    }

    private ShortenResponse buildResponse(Url url) {
        return ShortenResponse.builder()
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .longUrl(url.getLongUrl())
                .createdAt(url.getCreatedAt())
                .clickCount(url.getClickCount())
                .build();
    }
}
