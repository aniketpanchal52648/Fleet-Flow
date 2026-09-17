package com.example.matchingservice.service;

import com.example.matchingservice.dto.NearbyDriverDto;
import com.example.matchingservice.util.DatasegmentConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingClientService {

    private final RestTemplate restTemplate;

    @Value("${app.services.tracking-service-url:" + DatasegmentConstant.TRACKING_SERVICE_URL + "}")
    private String trackingServiceUrl;

    public List<NearbyDriverDto> getNearbyDrivers(Double latitude, Double longitude, Double radiusKm) {
        try {
            String url = String.format("%s/internal/v1/tracking-service/nearby-drivers?latitude=%f&longitude=%f&radiusKm=%f",
                    trackingServiceUrl, latitude, longitude, radiusKm);

            log.info("Calling Tracking Service for nearby drivers via Eureka: {}", url);
            ResponseEntity<List<NearbyDriverDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<NearbyDriverDto>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Tracking Service returned {} candidate driver(s)", response.getBody().size());
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to query nearby drivers from Tracking Service via Eureka: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
