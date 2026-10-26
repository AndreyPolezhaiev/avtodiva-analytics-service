package com.polezhaiev.analytics.repository.spec.booking;

import com.polezhaiev.analytics.model.SlotBooking;
import org.springframework.data.jpa.domain.Specification;

public class CarNameSpecification {
    public static Specification<SlotBooking> getSpecification(String carName) {
        return (root, query, cb) ->
                (carName == null || carName.isBlank())
                        ? null
                        : cb.equal(cb.lower(root.get("carName")), carName.toLowerCase().trim());
    }
}
