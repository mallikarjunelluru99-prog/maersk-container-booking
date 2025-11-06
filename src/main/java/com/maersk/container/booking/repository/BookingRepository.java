package com.maersk.container.booking.repository;

import com.maersk.container.booking.document.BookingDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends ReactiveMongoRepository<BookingDocument, String> {
}
