package com.nitesh.urlShortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UrlShortenerServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private ZSetOperations<String, String> zSetOps;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    @Test
    void testShortenUrl_ExistingUrl() {
        String longUrl = "https://www.test.com";
        String existingCode = "abc123";
        when(valueOps.get("url:" + longUrl)).thenReturn(existingCode);

        String shortUrl = urlShortenerService.shortenUrl(longUrl);

        assertEquals("http://localhost:8080/" + existingCode, shortUrl);
        verify(valueOps, never()).set(anyString(), anyString());
        verify(zSetOps, never()).incrementScore(anyString(), anyString(), anyDouble());
    }

    @Test
    void testGetLongUrl_Existing() {
        String shortCode = "abc123";
        String longUrl = "https://www.test.com";
        when(valueOps.get("code:" + shortCode)).thenReturn(longUrl);

        String result = urlShortenerService.getLongUrl(shortCode);

        assertEquals(longUrl, result);
    }

    @Test
    void testGetLongUrl_NonExisting() {
        String shortCode = "nonexistent";
        when(valueOps.get("code:" + shortCode)).thenReturn(null);

        String result = urlShortenerService.getLongUrl(shortCode);

        assertNull(result);
    }

    @Test
    void testGetTopDomains() {
        Set<String> topDomains = new LinkedHashSet<>();
        topDomains.add("example.com");
        topDomains.add("test.com");
        topDomains.add("sample.com");

        when(zSetOps.reverseRange("domain_counts", 0, 2)).thenReturn(topDomains);
        when(zSetOps.score("domain_counts", "example.com")).thenReturn(5.0);
        when(zSetOps.score("domain_counts", "test.com")).thenReturn(3.0);
        when(zSetOps.score("domain_counts", "sample.com")).thenReturn(2.0);

        Map<String, Integer> result = urlShortenerService.getTopDomains();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(5, result.get("example.com"));
        assertEquals(3, result.get("test.com"));
        assertEquals(2, result.get("sample.com"));
    }

    @Test
    void testGetTopDomains_Empty() {
        when(zSetOps.reverseRange("domain_counts", 0, 2)).thenReturn(null);

        Map<String, Integer> result = urlShortenerService.getTopDomains();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}