package io.ilot.plol.event;

import io.ilot.plol.model.Incident;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class IncidentApplicationEvent extends ApplicationEvent {
    private final Incident incident;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public IncidentApplicationEvent(Object source, Incident incident) {
        super(source);
        this.incident = incident;
    }
}
