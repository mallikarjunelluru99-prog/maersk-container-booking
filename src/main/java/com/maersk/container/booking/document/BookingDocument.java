package com.maersk.container.booking.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "bookings")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDocument {
    @Id
    private String bookingRef; // e.g., 957000001
    private String containerType;
    private Integer containerSize;
    private String origin;
    private String destination;
    private Integer quantity;
    private Instant timestamp;
}
