package com.nitesh.urlShortner.Controller;

import com.nitesh.urlShortner.controller.UrlShortenerController;
import com.nitesh.urlShortner.model.ShortenRequest;
import com.nitesh.urlShortner.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class UrlShortenerControllerTest {


    @Mock
    private URI uri;
    @Mock
    private UrlShortenerService urlShortenerService;

    @InjectMocks
    private UrlShortenerController urlShortenerController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testShortenUrl() {
        String longUrl = "http://test.com/longurl";
        String shortUrl = "http://localhost:8080/abc123";
        ShortenRequest request = new ShortenRequest();

        request.setUrl(longUrl);

        when(urlShortenerService.shortenUrl(longUrl)).thenReturn(shortUrl);

        ResponseEntity<Map<String, String>> response = urlShortenerController.shortenUrl(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("short_url", shortUrl), response.getBody());
    }

    @Test
    public void testRedirectToLongUrl_found() {
        String shortCode = "abc123";
        String longUrl = "http://test.com/longurl";

        when(urlShortenerService.getLongUrl(shortCode)).thenReturn(longUrl);

        ResponseEntity<Void> response = urlShortenerController.redirectToLongUrl(shortCode);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(uri.create(longUrl), response.getHeaders().getLocation());
    }

    @Test
    public void testRedirectToLongUrl_notFound() {
        String shortCode = "abc123";


        when(urlShortenerService.getLongUrl(shortCode)).thenReturn(null);

        ResponseEntity<Void> response = urlShortenerController.redirectToLongUrl(shortCode);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetMetrics() {
        Map<String, Integer> metrics = Map.of("test.com", 5);

        when(urlShortenerService.getTopDomains()).thenReturn(metrics);

        ResponseEntity<Map<String, Integer>> response = urlShortenerController.getMetrics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(metrics, response.getBody());
    }
}
