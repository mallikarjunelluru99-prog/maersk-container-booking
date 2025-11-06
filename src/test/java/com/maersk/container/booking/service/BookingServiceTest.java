package com.maersk.container.booking.service;


import com.maersk.container.booking.document.BookingDocument;
import com.maersk.container.booking.model.BookingRequest;
import com.maersk.container.booking.model.BookingResponse;
import com.maersk.container.booking.model.ContainerType;
import com.maersk.container.booking.repository.BookingRepository;
import com.maersk.container.booking.repository.BookingSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingSequenceGenerator sequenceGenerator;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBooking_success() {
        // Given
        BookingRequest request = BookingRequest.builder()
                .containerType(ContainerType.DRY)
                .containerSize(20)
                .origin("Chennai")
                .destination("Singapore")
                .quantity(5)
                .timestamp("2025-11-06T10:00:00Z")
                .build();

        when(sequenceGenerator.getNextSequence()).thenReturn(Mono.just("957000001"));
        when(bookingRepository.save(any(BookingDocument.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // When
        Mono<BookingResponse> result = bookingService.createBooking(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> assertThat(response.getBookingRef()).isEqualTo("957000001"))
                .verifyComplete();

        ArgumentCaptor<BookingDocument> bookingCaptor = ArgumentCaptor.forClass(BookingDocument.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getBookingRef()).isEqualTo("957000001");
    }

    @Test
    void createBooking_fails_logsError() {
        BookingRequest request = BookingRequest.builder()
                .containerType(ContainerType.DRY)
                .containerSize(20)
                .origin("Chennai")
                .destination("Singapore")
                .quantity(5)
                .timestamp("2025-11-06T10:00:00Z")
                .build();

        when(sequenceGenerator.getNextSequence()).thenReturn(Mono.just("957000002"));
        when(bookingRepository.save(any(BookingDocument.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(bookingService.createBooking(request))
                .expectErrorMatches(ex -> ex instanceof RuntimeException)
                .verify();

        verify(bookingRepository).save(any(BookingDocument.class));
    }
}
