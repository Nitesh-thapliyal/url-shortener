package com.nitesh.urlShortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UrlShortenerService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // characters used for creating short code
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    public String shortenUrl(String longUrl){

        if (longUrl == null || longUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL is required");
        }

        //check if the code exist in redis or not
        String existingCode = redisTemplate.opsForValue().get("url:" + longUrl);
        if(existingCode != null){
            return "http://localhost:8080/" + existingCode;
        }

        // if code not generated then generate
        String shortCode = generateShortCode();
        redisTemplate.opsForValue().set("url" + longUrl, shortCode);
        redisTemplate.opsForValue().set("code" + shortCode, longUrl);

        String domain = extractDomain(longUrl);
        redisTemplate.opsForZSet().incrementScore("domain_counts", domain, 1);

        return "http://localhost:8080/" + shortCode;
    }

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (Exception e) {
            return "";
        }
    }

    public String getLongUrl(String shortCode) {
        return redisTemplate.opsForValue().get("code:" + shortCode);
    }

    public Map<String, Integer> getTopDomains() {
        Set<String> topDomains = redisTemplate.opsForZSet().reverseRange("domain_counts", 0, 2);
        if (topDomains == null) {
            return Collections.emptyMap();
        }
        return topDomains.stream()
                .collect(Collectors.toMap(
                        domain -> domain,
                        domain -> Objects.requireNonNull(redisTemplate.opsForZSet().score("domain_counts", domain)).intValue(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

}
