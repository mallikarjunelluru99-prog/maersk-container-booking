package com.maersk.container.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingResponse {
    private String bookingRef;
    private String apiVersion = "v1";
}
