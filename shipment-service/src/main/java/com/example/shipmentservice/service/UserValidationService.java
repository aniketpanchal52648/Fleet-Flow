package com.example.shipmentservice.service;

import com.example.shipmentservice.util.DatasegmentConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.JsonNode;

import java.util.Objects;

@Service
@Slf4j
public class UserValidationService {

    @Autowired
    RestTemplate restTemplate;

    public boolean validateUserById(String userId){
        try {
            String url = DatasegmentConstant.USER_SERVICE_URL + "/web/v1/user-service/user/" + userId;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
        } catch (HttpClientErrorException.NotFound ex) {
            // Downstream service returned 404 -> User does not exist
            log.warn("User with ID [{}] not found in user-service", userId);
            return false;
        } catch (HttpStatusCodeException ex) {
            // Downstream returned 400 or other 4xx/5xx
            log.warn("user-service returned error status [{}]: {}", ex.getStatusCode(), ex.getMessage());
            return false;
        } catch (Exception ex) {
            // Network failure or service down
            log.error("Failed to connect to user-service: {}", ex.getMessage());
            return false;
        }
    }
}
