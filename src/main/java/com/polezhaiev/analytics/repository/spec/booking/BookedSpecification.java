package com.polezhaiev.analytics.repository.spec.booking;

import com.polezhaiev.analytics.model.SlotBooking;
import org.springframework.data.jpa.domain.Specification;

public class BookedSpecification {
    public static Specification<SlotBooking> getSpecification(Boolean booked) {
        return (root, query, cb) ->
                booked == null
                        ? null
                        : cb.equal(root.get("booked"), booked);
    }
}
