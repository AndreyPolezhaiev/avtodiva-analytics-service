package com.polezhaiev.analytics.service;

import com.polezhaiev.analytics.dto.CarProfitDto;
import com.polezhaiev.analytics.dto.InstructorProfitDto;
import com.polezhaiev.analytics.dto.ProfitResponseDto;
import com.polezhaiev.analytics.dto.ProfitSearchParametersDto;
import com.polezhaiev.analytics.dto.ScheduleSlotEventDto;
import com.polezhaiev.analytics.model.SlotBooking;
import com.polezhaiev.analytics.model.SlotEventType;
import com.polezhaiev.analytics.repository.SlotBookingRepository;
import com.polezhaiev.analytics.repository.spec.impl.SlotBookingSpecificationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotAnalyticsServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-20T10:00:00Z");
    private static final BigDecimal PRICE = new BigDecimal("500.00");

    @Mock
    private SlotBookingRepository slotBookingRepository;

    @Mock
    private SlotBookingSpecificationBuilder specificationBuilder;

    @InjectMocks
    private SlotAnalyticsService service;

    @Test
    void bookedEventSavesBookedSlotWithPrice() {
        when(slotBookingRepository.findById(1L)).thenReturn(Optional.empty());

        service.handleSlotEvent(event(SlotEventType.SLOT_BOOKED, true, NOW));

        SlotBooking saved = captureSaved();
        assertTrue(saved.isBooked());
        assertEquals(PRICE, saved.getLessonPrice());
        assertEquals("Skoda", saved.getCarName());
    }

    @Test
    void cancelledEventMarksSlotFree() {
        when(slotBookingRepository.findById(1L)).thenReturn(Optional.of(existing(true, NOW)));

        service.handleSlotEvent(event(SlotEventType.SLOT_CANCELLED, false, NOW.plusSeconds(1)));

        assertFalse(captureSaved().isBooked());
    }

    @Test
    void updateUsesBookedFlag() {
        when(slotBookingRepository.findById(1L)).thenReturn(Optional.of(existing(true, NOW)));

        service.handleSlotEvent(event(SlotEventType.SLOT_UPDATED, false, NOW.plusSeconds(1)));

        assertFalse(captureSaved().isBooked());
    }

    @Test
    void outdatedEventIsIgnored() {
        when(slotBookingRepository.findById(1L)).thenReturn(Optional.of(existing(false, NOW.plusSeconds(1))));

        service.handleSlotEvent(event(SlotEventType.SLOT_BOOKED, true, NOW));

        verify(slotBookingRepository, never()).save(any());
    }

    @Test
    void profitIsGroupedByCarAndInstructor() {
        ProfitSearchParametersDto searchParameters = new ProfitSearchParametersDto();
        searchParameters.setFrom(LocalDate.of(2026, 10, 1));
        searchParameters.setTo(LocalDate.of(2026, 10, 31));
        stubBookings(searchParameters, List.of(
                booked("Ivan", "Skoda", "500.00"),
                booked("Ivan", "Toyota", "650.00"),
                booked("Olena", "Skoda", "500.00")
        ));

        ProfitResponseDto profit = service.getProfit(searchParameters);

        assertEquals(3, profit.lessonsCount());
        assertEquals(new BigDecimal("1650.00"), profit.totalProfit());
        assertEquals(List.of(
                new CarProfitDto("Skoda", 2L, new BigDecimal("1000.00")),
                new CarProfitDto("Toyota", 1L, new BigDecimal("650.00"))
        ), profit.cars());
        assertEquals(List.of(
                new InstructorProfitDto("Ivan", 2L, new BigDecimal("1150.00")),
                new InstructorProfitDto("Olena", 1L, new BigDecimal("500.00"))
        ), profit.instructors());
    }

    @Test
    void profitByInstructorSumsAllCarsOfInstructor() {
        ProfitSearchParametersDto searchParameters = new ProfitSearchParametersDto();
        searchParameters.setInstructorName(" Ivan ");
        searchParameters.setCarName("  ");
        stubBookings(searchParameters, List.of(
                booked("Ivan", "Skoda", "500.00"),
                booked("Ivan", "Toyota", "650.00")
        ));

        ProfitResponseDto profit = service.getProfit(searchParameters);

        assertEquals("Ivan", profit.instructorName());
        assertNull(profit.carName());
        assertEquals(2, profit.lessonsCount());
        assertEquals(new BigDecimal("1150.00"), profit.totalProfit());
        assertEquals(2, profit.cars().size());
    }

    @Test
    void profitHandlesMissingPriceAndName() {
        ProfitSearchParametersDto searchParameters = new ProfitSearchParametersDto();
        stubBookings(searchParameters, List.of(booked("Ivan", null, null)));

        ProfitResponseDto profit = service.getProfit(searchParameters);

        assertEquals(BigDecimal.ZERO, profit.totalProfit());
        assertEquals(List.of(new CarProfitDto(null, 1L, BigDecimal.ZERO)), profit.cars());
    }

    @SuppressWarnings("unchecked")
    private void stubBookings(ProfitSearchParametersDto searchParameters, List<SlotBooking> bookings) {
        Specification<SlotBooking> spec = mock(Specification.class);
        when(specificationBuilder.build(searchParameters)).thenReturn(spec);
        when(slotBookingRepository.findAll(spec)).thenReturn(bookings);
    }

    private SlotBooking booked(String instructorName, String carName, String price) {
        SlotBooking booking = new SlotBooking();
        booking.setInstructorName(instructorName);
        booking.setCarName(carName);
        booking.setLessonPrice(price != null ? new BigDecimal(price) : null);
        booking.setBooked(true);
        return booking;
    }

    private SlotBooking captureSaved() {
        ArgumentCaptor<SlotBooking> captor = ArgumentCaptor.forClass(SlotBooking.class);
        verify(slotBookingRepository).save(captor.capture());
        return captor.getValue();
    }

    private SlotBooking existing(boolean booked, Instant lastEventTimestamp) {
        SlotBooking booking = new SlotBooking();
        booking.setSlotId(1L);
        booking.setBooked(booked);
        booking.setLastEventTimestamp(lastEventTimestamp);
        return booking;
    }

    private ScheduleSlotEventDto event(SlotEventType type, boolean booked, Instant timestamp) {
        return new ScheduleSlotEventDto("event-" + type, type, timestamp,
                1L, LocalDate.of(2026, 10, 21), null, null,
                "Instructor", "Skoda", booked ? "Student" : null,
                PRICE, booked);
    }
}
