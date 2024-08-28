package com.nitesh.urlShortner.controller;

import com.nitesh.urlShortner.model.ShortenRequest;
import com.nitesh.urlShortner.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public ResponseEntity<Map<String, String>> shortenUrl(@RequestBody ShortenRequest request) {
        String shortUrl = urlShortenerService.shortenUrl(request.getUrl());
        return ResponseEntity.ok(Map.of("short_url", shortUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortCode) {
        String longUrl = urlShortenerService.getLongUrl(shortCode);
        if (longUrl != null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(longUrl))
                    .build();
        }
        return ResponseEntity.notFound().build();
    }
}
