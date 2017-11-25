package io.ilot.plol.event;

import io.ilot.plol.repos.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PlolEventPublisher {
    private Logger logger = LoggerFactory.getLogger(PlolEventPublisher.class);

    @Autowired
    private IncidentRepository ir;

    @Autowired
    private SimpMessagingTemplate template;

    public void play(){
        ir.findAll(new Sort(new Sort.Order(Sort.Direction.ASC,"when")))
                .stream()
                .peek(e -> logger.info(e.toString()))
                .forEach(incident -> template.convertAndSend("/topic/play", incident));
    }

}
