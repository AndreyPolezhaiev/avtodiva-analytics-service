package com.polezhaiev.analytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Last known state of a slot from avtodiva-advanced.
 * Lesson price is a snapshot taken at the time of the event
 */
@Entity
@Table(name = "slot_bookings")
@Data
public class SlotBooking {
    @Id
    @Column(name = "slot_id")
    private Long slotId;

    private LocalDate date;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "instructor_name")
    private String instructorName;

    @Column(name = "car_name")
    private String carName;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "lesson_price", precision = 10, scale = 2)
    private BigDecimal lessonPrice;

    @Column(nullable = false)
    private boolean booked;

    @Column(name = "last_event_id")
    private String lastEventId;

    @Column(name = "last_event_timestamp")
    private Instant lastEventTimestamp;
}
