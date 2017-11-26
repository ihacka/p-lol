package io.ilot.plol.controller;


import io.ilot.plol.ParserLiveOdds;
import io.ilot.plol.ParserLiveScout;
import io.ilot.plol.event.IncidentApplicationEvent;
import io.ilot.plol.event.PlolEventPublisher;
import io.ilot.plol.model.Bet;
import io.ilot.plol.model.Event;
import io.ilot.plol.model.Incident;
import io.ilot.plol.event.PlolEventPublisher;
import io.ilot.plol.model.Bet;
import io.ilot.plol.model.User;
import io.ilot.plol.repos.BetRepository;
import io.ilot.plol.repos.EventRepository;
import io.ilot.plol.repos.IncidentRepository;
import io.ilot.plol.repos.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
class Controller /*implements ApplicationListener<IncidentApplicationEvent>*/ {
    private Logger logger = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BetRepository betRepository;
    @Autowired
    private IncidentRepository incidentRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ParserLiveScout parserLiveScout;
    @Autowired
    private ParserLiveOdds parserLiveOdds;
    @Autowired
    PlolEventPublisher plolEventPublisher;


    @RequestMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        return "Hello, " + name;
    }

    @RequestMapping("/users")
    public ResponseEntity<List<User>> users() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @RequestMapping("/events")
    public ResponseEntity<List<Event>> events() {
        return ResponseEntity.ok(eventRepository.findAll());
    }

    @RequestMapping("/incidents")
    public ResponseEntity<List<Incident>> incidents() {
        return ResponseEntity.ok(incidentRepository.findAll());
    }

    @RequestMapping(value = "/bets", method = RequestMethod.GET)
    public ResponseEntity<List<Bet>> placedBets(){
        return ResponseEntity.ok(betRepository.findAll());
    }

    @RequestMapping(value = "/bets/{id}", method = RequestMethod.GET)
    public ResponseEntity<Bet> placedBets(@PathVariable Long id){
        return ResponseEntity.ok(betRepository.findOne(id));
    }

    @RequestMapping(value = "/bets/userbets/{userid}", method = RequestMethod.GET)
    public ResponseEntity<List<Bet>> userBets(@PathVariable Long userid){
        User user = userRepository.findOne(userid);
        return ResponseEntity.ok(betRepository.findByUser(user));
    }

    @RequestMapping(value = "/bets", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Bet> placeBet(@RequestBody Bet bet, @RequestHeader(name = "userId", defaultValue = "1") Long userId){
        final User user = userRepository.findOne(userId);
        bet.setUser(user);

        //calculate winnings
        bet.setWinnings(new BigDecimal(10));
        user.removeFromBalance(bet.getStake());

        return ResponseEntity.status(HttpStatus.CREATED).body(betRepository.save(bet));
    }

    @RequestMapping("/play/start")
    public void startPlay() {
        plolEventPublisher.play();
    }


    @RequestMapping("parser/scout/start")
    public void startScoutParser() {
        try {
            parserLiveScout.startParser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("parser/odds/start")
    public void startOddsParser() {
        try {
            parserLiveOdds.startParser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
