package com.polezhaiev.analytics.controller;

import com.polezhaiev.analytics.dto.ProfitResponseDto;
import com.polezhaiev.analytics.dto.ProfitSearchParametersDto;
import com.polezhaiev.analytics.service.SlotAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class SlotAnalyticsController {
    private final SlotAnalyticsService slotAnalyticsService;

    /**
     * Profit of booked lessons grouped by car and by instructor, both dates are optional and inclusive.
     * Optional instructorName and carName narrow the result to that instructor and/or car
     */
    @GetMapping("/profit")
    @PreAuthorize("hasRole('ADMIN')")
    public ProfitResponseDto getProfit(ProfitSearchParametersDto searchParameters) {
        return slotAnalyticsService.getProfit(searchParameters);
    }
}
