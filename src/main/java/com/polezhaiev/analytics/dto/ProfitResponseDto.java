package com.polezhaiev.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProfitResponseDto(
    LocalDate from,
    LocalDate to,
    String instructorName,
    String carName,
    long lessonsCount,
    BigDecimal totalProfit,
    List<CarProfitDto> cars,
    List<InstructorProfitDto> instructors
) {}
