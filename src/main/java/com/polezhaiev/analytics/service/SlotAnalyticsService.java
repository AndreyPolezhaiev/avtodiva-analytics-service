package com.polezhaiev.analytics.service;

import com.polezhaiev.analytics.dto.CarProfitDto;
import com.polezhaiev.analytics.dto.InstructorProfitDto;
import com.polezhaiev.analytics.dto.ProfitResponseDto;
import com.polezhaiev.analytics.dto.ProfitSearchParametersDto;
import com.polezhaiev.analytics.dto.ScheduleSlotEventDto;
import com.polezhaiev.analytics.model.SlotBooking;
import com.polezhaiev.analytics.repository.SlotBookingRepository;
import com.polezhaiev.analytics.repository.spec.impl.SlotBookingSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlotAnalyticsService {
    private final SlotBookingRepository slotBookingRepository;
    private final SlotBookingSpecificationBuilder specificationBuilder;

    /**
     * Stores the latest state of the slot, so duplicated
     * and out-of-order events don't break the profit
     *
     * @param event
     */
    @Transactional
    public void handleSlotEvent(ScheduleSlotEventDto event) {
        if (event.slotId() == null || event.slotEventType() == null) {
            log.warn("Skipping invalid slot event {}: slotId or slotEventType is missing", event.eventId());
            return;
        }

        SlotBooking booking = slotBookingRepository.findById(event.slotId()).orElseGet(SlotBooking::new);
        if (isOutdated(event, booking)) {
            log.info("Skipping outdated slot event {} for slot {}", event.eventId(), event.slotId());
            return;
        }

        booking.setSlotId(event.slotId());
        booking.setDate(event.date());
        booking.setStartTime(event.startTime());
        booking.setEndTime(event.endTime());
        booking.setInstructorName(event.instructorName());
        booking.setCarName(event.carName());
        booking.setStudentName(event.studentName());
        booking.setLessonPrice(event.lessonPrice());
        booking.setBooked(resolveBooked(event));
        booking.setLastEventId(event.eventId());
        booking.setLastEventTimestamp(event.timestamp());

        slotBookingRepository.save(booking);
    }

    /**
     * Profit for the period, optionally narrowed to one instructor and/or one car.
     * The result is broken down both by car and by instructor
     */
    @Transactional(readOnly = true)
    public ProfitResponseDto getProfit(ProfitSearchParametersDto searchParameters) {
        Specification<SlotBooking> bookingSpecification = specificationBuilder.build(searchParameters);
        List<SlotBooking> bookings = slotBookingRepository.findAll(bookingSpecification);

        List<CarProfitDto> cars = groupProfit(bookings, SlotBooking::getCarName, CarProfitDto::new);
        List<InstructorProfitDto> instructors =
                groupProfit(bookings, SlotBooking::getInstructorName, InstructorProfitDto::new);

        BigDecimal totalProfit = sumPrices(bookings);

        return new ProfitResponseDto(
                searchParameters.getFrom(),
                searchParameters.getTo(),
                normalize(searchParameters.getInstructorName()),
                normalize(searchParameters.getCarName()),
                bookings.size(),
                totalProfit,
                cars,
                instructors
        );
    }

    /**
     * Groups bookings by the given name, the most profitable go first.
     * HashMap is used because names can be null
     */
    private <D> List<D> groupProfit(List<SlotBooking> bookings,
                                    Function<SlotBooking, String> nameExtractor,
                                    ProfitDtoFactory<D> dtoFactory) {
        Map<String, List<SlotBooking>> groups = new HashMap<>();
        bookings.forEach(booking ->
                groups.computeIfAbsent(nameExtractor.apply(booking), name -> new ArrayList<>()).add(booking));

        return groups.entrySet().stream()
                .map(group -> new ProfitGroup(group.getKey(), group.getValue().size(), sumPrices(group.getValue())))
                .sorted(Comparator.comparing(ProfitGroup::profit).reversed())
                .map(group -> dtoFactory.create(group.name(), group.lessonsCount(), group.profit()))
                .toList();
    }

    private BigDecimal sumPrices(List<SlotBooking> bookings) {
        return bookings.stream()
                .map(this::priceOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal priceOf(SlotBooking booking) {
        return booking.getLessonPrice() != null ? booking.getLessonPrice() : BigDecimal.ZERO;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record ProfitGroup(String name, long lessonsCount, BigDecimal profit) {}

    @FunctionalInterface
    private interface ProfitDtoFactory<D> {
        D create(String name, Long lessonsCount, BigDecimal profit);
    }

    private boolean resolveBooked(ScheduleSlotEventDto event) {
        return switch (event.slotEventType()) {
            case SLOT_BOOKED -> true;
            case SLOT_CANCELLED -> false;
            // an update can either assign or remove the student
            case SLOT_UPDATED -> Boolean.TRUE.equals(event.booked());
        };
    }

    private boolean isOutdated(ScheduleSlotEventDto event, SlotBooking booking) {
        return event.timestamp() != null
                && booking.getLastEventTimestamp() != null
                && event.timestamp().isBefore(booking.getLastEventTimestamp());
    }
}
