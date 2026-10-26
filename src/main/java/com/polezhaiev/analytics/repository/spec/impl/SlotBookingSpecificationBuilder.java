package com.polezhaiev.analytics.repository.spec.impl;

import com.polezhaiev.analytics.dto.ProfitSearchParametersDto;
import com.polezhaiev.analytics.model.SlotBooking;
import com.polezhaiev.analytics.repository.spec.SpecificationBuilder;
import com.polezhaiev.analytics.repository.spec.booking.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Only booked slots bring profit, so the booked filter is always applied
 */
@Component
public class SlotBookingSpecificationBuilder implements SpecificationBuilder<SlotBooking, ProfitSearchParametersDto> {
    @Override
    public Specification<SlotBooking> build(ProfitSearchParametersDto searchParameters) {
        Specification<SlotBooking> spec = Specification.allOf(BookedSpecification.getSpecification(true));

        if (searchParameters.getInstructorName() != null && !searchParameters.getInstructorName().isBlank()) {
            spec = spec.and(InstructorNameSpecification.getSpecification(searchParameters.getInstructorName()));
        }
        if (searchParameters.getCarName() != null && !searchParameters.getCarName().isBlank()) {
            spec = spec.and(CarNameSpecification.getSpecification(searchParameters.getCarName()));
        }
        if (searchParameters.getFrom() != null || searchParameters.getTo() != null) {
            spec = spec.and(BookingDateRangeSpecification.getSpecification(
                    searchParameters.getFrom(),
                    searchParameters.getTo()
            ));
        }

        return spec;
    }
}
