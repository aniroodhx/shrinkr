package com.shrinkr.controller;

import com.shrinkr.dto.ShortenRequest;
import com.shrinkr.dto.ShortenResponse;
import com.shrinkr.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        log.info("Shorten request received for: {}", request.getLongUrl());
        ShortenResponse response = urlService.shortenUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        log.info("Redirect request for shortCode={}", shortCode);
        String longUrl = urlService.getLongUrl(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, longUrl);

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<ShortenResponse> getStats(@PathVariable String shortCode) {
        log.info("Stats request for shortCode={}", shortCode);
        return ResponseEntity.ok(urlService.getUrlStats(shortCode));
    }
}
