package com.maersk.container.booking.service;

import com.maersk.container.booking.document.BookingDocument;
import com.maersk.container.booking.exception.BookingException;
import com.maersk.container.booking.model.BookingRequest;
import com.maersk.container.booking.model.BookingResponse;
import com.maersk.container.booking.repository.BookingRepository;
import com.maersk.container.booking.repository.BookingSequenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository repository;
    private final BookingSequenceGenerator sequenceGenerator;

    public Mono<BookingResponse> createBooking(BookingRequest request) {
        return sequenceGenerator.getNextSequence()
                .flatMap(seq -> {
                    String bookingRef = String.valueOf(seq);

                    BookingDocument doc = BookingDocument.builder()
                            .bookingRef(bookingRef)
                            .containerType(request.getContainerType().name())
                            .containerSize(request.getContainerSize())
                            .origin(request.getOrigin())
                            .destination(request.getDestination())
                            .quantity(request.getQuantity())
                            .timestamp(Instant.parse(request.getTimestamp()))
                            .build();

                    return repository.save(doc)
                            .map(saved -> new BookingResponse(saved.getBookingRef()));
                })
                .onErrorResume(ex -> {
                    log.error("Booking creation failed", ex);
                    return Mono.error(new BookingException());
                });
    }
}
