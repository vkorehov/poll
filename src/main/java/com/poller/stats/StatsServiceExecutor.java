package com.poller.stats;

import io.micronaut.context.annotation.Requires;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

@Singleton
@Requires(notEnv = "test")// disable periodic execution during unit tests to avoid races during context shutdown
public class StatsServiceExecutor {
    private final StatsService statsService;

    public StatsServiceExecutor(StatsService statsService) {
        this.statsService = statsService;
    }

    @Scheduled(fixedDelay = "1s")
    public void calculateScalarStats() {
        this.statsService.calculateScalarStats();
    }

    @Scheduled(fixedDelay = "1s")
    public void calculateTextStats() {
        this.statsService.calculateTextStats();
    }
}
