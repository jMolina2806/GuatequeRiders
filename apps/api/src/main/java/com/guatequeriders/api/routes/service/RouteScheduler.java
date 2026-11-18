package com.guatequeriders.api.routes.service;

import com.guatequeriders.api.circuits.service.CircuitActivitiesService;
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
    private final CircuitActivitiesService circuitActivitiesService;

    public RouteScheduler(RoutesService routesService, CircuitActivitiesService circuitActivitiesService) {
        this.routesService = routesService;
        this.circuitActivitiesService = circuitActivitiesService;
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
        int completedRoutes = routesService.completePastRoutes();
        if (completedRoutes > 0) {
            log.info("Auto-completed {} past routes on {}", completedRoutes, trigger);
        }

        int completedCircuitActivities = circuitActivitiesService.completePastCircuitActivities();
        if (completedCircuitActivities > 0) {
            log.info("Auto-completed {} past circuit activities on {}", completedCircuitActivities, trigger);
        }
    }
}
