package com.example.trackingservice.repository;

import com.example.trackingservice.documents.DriverLocationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverLocationMongoRepository extends MongoRepository<DriverLocationDocument, String> {
    boolean existsByEventId(String eventId);
    Optional<DriverLocationDocument> findFirstByDriverIdOrderByRecordedAtDesc(String driverId);
}
