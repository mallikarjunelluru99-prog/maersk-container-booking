package com.maersk.container.booking.repository;

import com.maersk.container.booking.document.BookingSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BookingSequenceGenerator {

    private final ReactiveMongoOperations mongoOps;
    private static final String SEQUENCE_NAME = "booking_seq";
    private static final long START_AT = 957_000_000L;

    public Mono<String> getNextSequence() {
        Query query = Query.query(Criteria.where("_id").is(SEQUENCE_NAME));

        Update incUpdate = new Update().inc("value", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options()
                .returnNew(true)
                .upsert(false);

        // ensure both branches return Mono<String>
        return mongoOps.findAndModify(query, incUpdate, options, BookingSequence.class)
                .map(seq -> String.format("%09d", seq.getValue()))
                .switchIfEmpty(initializeSequence());
    }

    private Mono<String> initializeSequence() {
        BookingSequence newSeq = new BookingSequence();
        newSeq.setId(SEQUENCE_NAME);
        newSeq.setValue(START_AT + 1);

        return mongoOps.save(newSeq)
                .map(saved -> String.format("%09d", saved.getValue()));
    }
}
