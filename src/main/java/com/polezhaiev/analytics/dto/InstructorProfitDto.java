package com.polezhaiev.analytics.dto;

import java.math.BigDecimal;

public record InstructorProfitDto(
    String instructorName,
    Long lessonsCount,
    BigDecimal profit
) {}
