package io.ilot.plol.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketsController {
    final Logger logger = LoggerFactory.getLogger(WebSocketsController.class);

    public enum WebSocketTopic {
        PLAY("/topic/play");

        private String topicName;
        WebSocketTopic(String topicName){
            this.topicName = topicName;
        }
        public String topicName(){
            return topicName;
        }
    }

    //this would create an endpoint for clients to send Incident type of data

//    @MessageMapping("/incident")
//    public Incident accept(Incident incident) {
//        logger.info("pushing {}", incident);
//        return incident;
//    }
}
