package com.eventhub.scheduler;

import com.eventhub.entity.Event;
import com.eventhub.enums.EventStatus;
import com.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    // 60 sec
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void finishPastEvents() {
        List<Event> pastPublishedEvents = eventRepository.findByStatusAndEndDateBefore(EventStatus.PUBLISHED, LocalDateTime.now());

        if (pastPublishedEvents.isEmpty()) {
            return;
        }

        pastPublishedEvents.forEach(event -> event.setStatus(EventStatus.FINISHED));

        log.info("Finished {} past published events", pastPublishedEvents.size());
    }
}