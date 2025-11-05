package com.maersk.container.booking.model;

import lombok.Data;

@Data
public class AvailabilityRequest {
    private String containerType;
    private int containerSize;
    private String origin;
    private String destination;
    private int quantity;
}
