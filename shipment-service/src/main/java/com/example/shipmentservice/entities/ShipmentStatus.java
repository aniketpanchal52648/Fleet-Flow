package com.example.shipmentservice.entities;

public enum ShipmentStatus {

    CREATED,
    AWAITING_DRIVER,
    DRIVER_ASSIGNED,
    PICKED_UP,
    IN_TRANSIT,
    COMPLETED,
    CANCELLED
}
