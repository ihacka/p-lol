package io.ilot.plol;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;

@SpringBootApplication
public class PlolApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlolApplication.class, args);
	}

	@Bean
    CommandLineRunner UsersInitializer (UserRepository ur) {
        return args ->
            Stream.of(
                    new User("Andreas", "andreas@ilot.io", new BigDecimal(10000)),
                    new User("Nikos", "nikos@ilot.io", new BigDecimal(10000)),
                    new User("Eleni", "eleni@ilot.io", new BigDecimal(10000)),
                    new User("Christos", "christos@ilot.io", new BigDecimal(10000))
            ).forEach(ur::save);
    }

    @Bean
    CommandLineRunner Incidentinitializer (IncidentRepository ir) {
        final Function<LocalDateTime, Date> asDate = ldt -> Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        return args -> {
            Stream.of(
                    new Incident(123l, IncidentType.KICKOFF, null, asDate.apply(now())),
                    new Incident(123l, IncidentType.CORNER, ParticipantType.HOME, asDate.apply(now().plusSeconds(10))),
                    new Incident(123l, IncidentType.CORNER, ParticipantType.AWAY, asDate.apply(now().plusSeconds(10))),
                    new Incident(123l, IncidentType.YELLOW_CARD, ParticipantType.AWAY, asDate.apply(now().plusSeconds(10))),
                    new Incident(123l, IncidentType.GOAL, ParticipantType.HOME, asDate.apply(now().plusSeconds(10)))
            ).forEach(ir::save);
        };
    }

}


@RestController
class Controller {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BetRepository betRepository;
    @Autowired
    private IncidentRepository incidentRepository;

    @RequestMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        return "Hello, " + name;
    }

    @RequestMapping("/users")
    public ResponseEntity<List<User>> users() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @RequestMapping(value = "/bets", method = RequestMethod.GET)
    public ResponseEntity<List<Bet>> placedBets(){
        return ResponseEntity.ok(betRepository.findAll());
    }

    @RequestMapping(value = "/bets/{id}", method = RequestMethod.GET)
    public ResponseEntity<Bet> placedBets(@PathVariable Long id){
        return ResponseEntity.ok(betRepository.findOne(id));
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


    @MessageMapping("/play")
    @SendTo("/topic/play")
    public Incident play(Long eventId) throws Exception {
        Thread.sleep(1000);
        return new Incident();
    }

    private void delaydas(long time){
    }

}

interface UserRepository extends JpaRepository<User, Long> {

    //@Query("from users where name = :name")
    Collection<User> findByName(String name);
}

interface BetRepository extends JpaRepository<Bet, Long> {
}

interface IncidentRepository extends JpaRepository<Incident, Long> {}

@Entity @Data @NoArgsConstructor class User {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String email;
    private BigDecimal balance;

    public User(String name, String email, BigDecimal balance) {
        this.name = name;
        this.email = email;
        this.balance = balance;
    }

    public BigDecimal removeFromBalance(BigDecimal amount){
        if(amount.compareTo(balance) > 1) throw new IllegalArgumentException("Insufficient funds");
        setBalance(balance.subtract(amount));
        return balance;
    }
}

@Entity @Data @NoArgsConstructor class Bet {
    @Id @GeneratedValue
    private Long id;
    private Long selectionId;
    private BigDecimal stake;
    @Transient @Setter(AccessLevel.NONE) private BigDecimal payout;
    private BigDecimal winnings;
    private Date placedTime = new Date();
    @OneToOne private User user;

    public BigDecimal getPayout() {
        return Optional.ofNullable(payout).map(p -> p.subtract(stake)).orElse(BigDecimal.ZERO);
    }
}

@Entity @Data @NoArgsConstructor class Event {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(mappedBy = "event")
    private List<Market> markets;
}

@Entity @Data @NoArgsConstructor class Market {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @ManyToOne @JoinColumn(name = "event_id")
    private Event event;

}



enum IncidentType {
        GOAL,  KICKOFF, CORNER, YELLOW_CARD, RED_CARD,
}
enum ParticipantType {
        HOME, AWAY
}

@Entity @Data @NoArgsConstructor class Incident {
    @Id @GeneratedValue
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




