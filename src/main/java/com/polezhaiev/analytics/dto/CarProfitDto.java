package com.polezhaiev.analytics.dto;

import java.math.BigDecimal;

public record CarProfitDto(
    String carName,
    Long lessonsCount,
    BigDecimal profit
) {}
