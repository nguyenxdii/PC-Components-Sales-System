package com.diiexe.pcsalessystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api-keys}")
    private String apiKeysString;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.url}")
    private String urlTemplate;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);

    public String generateContent(String prompt) {
        List<String> keys = Arrays.asList(apiKeysString.split(","));
        int attempts = keys.size();

        for (int i = 0; i < attempts; i++) {
            int index = (currentKeyIndex.get() + i) % keys.size();
            String key = keys.get(index).trim();
            String url = urlTemplate.replace("{model}", model).replace("{key}", key);

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                        Map.of("parts", List.of(
                            Map.of("text", prompt)
                        ))
                    )
                );

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                
                log.info("Calling Gemini API with key index: {}", index);
                String response = restTemplate.postForObject(url, entity, String.class);
                
                // Update the current key index to the successful one for future calls
                currentKeyIndex.set(index);
                return response;

            } catch (Exception e) {
                log.error("Error calling Gemini API with key index {}: {}", index, e.getMessage());
                // If it's a rate limit or other error, try next key
            }
        }

        throw new RuntimeException("All Gemini API keys failed after " + attempts + " attempts.");
    }
}
