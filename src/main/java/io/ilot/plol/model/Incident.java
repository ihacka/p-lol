package io.ilot.plol.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Incident {
    @Id
    @GeneratedValue
    private Long id;
    private Long eventId;
    private IncidentType incidentType;
    private ParticipantType participantType;
    private Date when = new Date();

    public Incident(Long eventId, IncidentType incidentType, ParticipantType participantType, Date when) {
        this.eventId = eventId;
        this.incidentType = incidentType;
        this.participantType = participantType;
        this.when = when;
    }
}
