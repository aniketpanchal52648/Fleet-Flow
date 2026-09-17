package com.example.trackingservice.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Document(collection = "driver_location_events")
@CompoundIndex(name = "driver_recorded_idx", def = "{'driverId': 1, 'recordedAt': -1}")
@CompoundIndex(name = "session_recorded_idx", def = "{'sessionId': 1, 'recordedAt': -1}")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverLocationDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String eventId;

    private String driverId;
    private String vehicleId;
    private String sessionId;

    private Double latitude;
    private Double longitude;
    private Double accuracyMeters;
    private Double speedKph;
    private Double bearing;

    private LocalDateTime recordedAt;
    private Instant receivedAt;

    private String activeDeliveryJobId;
    private String source;
}
