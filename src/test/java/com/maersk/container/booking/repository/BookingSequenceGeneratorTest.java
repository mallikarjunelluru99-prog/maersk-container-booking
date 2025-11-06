package com.maersk.container.booking.repository;

import com.maersk.container.booking.document.BookingSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class BookingSequenceGeneratorTest {

    @Mock
    private ReactiveMongoOperations mongoOps;

    @InjectMocks
    private BookingSequenceGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldStartAt957000001_WhenFirstCall() {
        when(mongoOps.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(BookingSequence.class)))
                .thenReturn(Mono.empty());
        when(mongoOps.save(any(BookingSequence.class))).thenReturn(Mono.just(new BookingSequence("booking_seq", 957000001L)));

        StepVerifier.create(generator.getNextSequence())
                .expectNext("957000001")
                .verifyComplete();
    }

    @Test
    void shouldIncrementValue_WhenSubsequentCall() {
        BookingSequence existing = new BookingSequence();
        existing.setId("booking_seq");
        existing.setValue(957000001L);

        when(mongoOps.save(any(BookingSequence.class))).thenReturn(Mono.just(existing));
        when(mongoOps.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(BookingSequence.class)
        )).thenReturn(Mono.just(existing));

        StepVerifier.create(generator.getNextSequence())
                .expectNext("957000001")
                .verifyComplete();
    }
}
