package com.example.shipmentservice.service;

import com.example.shipmentservice.util.DatasegmentConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.JsonNode;

import java.util.Objects;

@Service
@Slf4j
public class UserValidationService {

    @Autowired
    RestTemplate restTemplate;

    public boolean validateUserById(String userId){
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = DatasegmentConstant.USER_SERVICE_URL + "/web/v1/user-service/user/" + userId;
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                url,
                JsonNode.class
        );

        if (response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null) {
                return true;
            // User exists

        } else {
            log.error("User not found: "+userId);
            log.error(response.getStatusCode().toString());
//            log.error(Objects.requireNonNull(response.getBody()).toString());
            return false;


            // User doesn't exist
        }
    }
}
