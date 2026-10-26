package com.polezhaiev.analytics.repository.spec.booking;

import com.polezhaiev.analytics.model.SlotBooking;
import org.springframework.data.jpa.domain.Specification;

public class InstructorNameSpecification {
    public static Specification<SlotBooking> getSpecification(String instructorName) {
        return (root, query, cb) ->
                (instructorName == null || instructorName.isBlank())
                        ? null
                        : cb.equal(cb.lower(root.get("instructorName")), instructorName.toLowerCase().trim());
    }
}
