package com.portfolio.hms.appointment.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMetrics {

    private final Counter scheduledCounter;
    private final Counter cancelledCounter;
    private final Counter completedCounter;
    private final Counter conflictsCounter;
    private final Counter noShowCounter;

    public AppointmentMetrics(MeterRegistry registry) {
        this.scheduledCounter = registry.counter("appointments.scheduled");
        this.cancelledCounter = registry.counter("appointments.cancelled");
        this.completedCounter = registry.counter("appointments.completed");
        this.conflictsCounter = registry.counter("appointments.conflicts");
        this.noShowCounter = registry.counter("appointments.noshow");
    }

    public void incrementScheduled() { scheduledCounter.increment(); }
    public void incrementCancelled() { cancelledCounter.increment(); }
    public void incrementCompleted() { completedCounter.increment(); }
    public void incrementConflicts() { conflictsCounter.increment(); }
    public void incrementNoShow() { noShowCounter.increment(); }
}
