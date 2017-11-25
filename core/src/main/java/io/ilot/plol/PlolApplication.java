package io.ilot.plol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.ilot.plol.model.*;
import io.ilot.plol.repos.EventRepository;
import io.ilot.plol.repos.IncidentRepository;
import io.ilot.plol.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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



    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(mapper);
        return converter;
    }
}




