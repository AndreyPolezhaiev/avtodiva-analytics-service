package com.polezhaiev.analytics.dto;

import com.polezhaiev.analytics.model.SlotEventType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleSlotEventDto(
    String eventId,
    SlotEventType slotEventType,
    Instant timestamp,

    Long slotId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,

    String instructorName,
    String carName,
    String studentName,

    BigDecimal lessonPrice,

    Boolean booked
) {}
