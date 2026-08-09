package com.guatequeriders.api.routes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RouteScheduler {

    private static final Logger log = LoggerFactory.getLogger(RouteScheduler.class);

    private final RoutesService routesService;

    public RouteScheduler(RoutesService routesService) {
        this.routesService = routesService;
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void completePastRoutesHourly() {
        completePastRoutes("scheduled");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void completePastRoutesOnStartup() {
        completePastRoutes("startup");
    }

    private void completePastRoutes(String trigger) {
        int updated = routesService.completePastRoutes();
        if (updated > 0) {
            log.info("Auto-completed {} past routes on {}", updated, trigger);
        }
    }
}
