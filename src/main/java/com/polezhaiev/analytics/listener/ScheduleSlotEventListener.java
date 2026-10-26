package com.polezhaiev.analytics.listener;

import com.polezhaiev.analytics.dto.ScheduleSlotEventDto;
import com.polezhaiev.analytics.service.SlotAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleSlotEventListener {
    private final SlotAnalyticsService slotAnalyticsService;

    @KafkaListener(topics = "${app.kafka.topics.analytics-slots}")
    public void onSlotEvent(ScheduleSlotEventDto event) {
        log.info("Received slot analytics event {} ({}) for slot {}",
                event.eventId(), event.slotEventType(), event.slotId());

        slotAnalyticsService.handleSlotEvent(event);
    }
}
