package com.maersk.container.booking.model;

import lombok.Data;

@Data
public class BookingRequest extends AvailabilityRequest {
    private String timestamp;
}
