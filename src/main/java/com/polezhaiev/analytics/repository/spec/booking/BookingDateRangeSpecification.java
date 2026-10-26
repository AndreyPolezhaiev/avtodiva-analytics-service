package com.polezhaiev.analytics.repository.spec.booking;

import com.polezhaiev.analytics.model.SlotBooking;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class BookingDateRangeSpecification {
    public static Specification<SlotBooking> getSpecification(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("date"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("date"), from);
            return cb.lessThanOrEqualTo(root.get("date"), to);
        };
    }
}
