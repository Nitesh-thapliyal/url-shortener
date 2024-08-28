package com.nitesh.urlShortner.controller;

import com.nitesh.urlShortner.model.ShortenRequest;
import com.nitesh.urlShortner.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
